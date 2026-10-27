# Query-expansion semantic bundles

> Status: design draft; not yet implemented.

## Motivation

Query expansion currently creates one composite concept for every returned semantic-relation edge. Each composite therefore expresses only the direct translation of that edge through the configured TOP query-expansion strategy.

For example, these relations:

```text
myocardial infarction --treated_by--> aspirin
myocardial infarction --treated_by--> clopidogrel
```

currently produce two composites:

```text
AND(myocardial infarction, aspirin)
AND(myocardial infarction, clopidogrel)
```

This is mechanically correct but repetitive. Query expansion should instead create fewer, more expressive **semantic bundles**, such as:

```text
AND(
  myocardial infarction,
  OR(aspirin, clopidogrel)
)
```

The objective is:

> Compile a coherent semantic-relation subgraph into one expressive composite concept instead of compiling every relation edge into a separate composite concept.

## Bundle boundary

Bundles should be formed separately for each:

1. selected relation mapping;
2. semantic relation ID; and
3. connected component of the resolved relation graph.

The selected mapping must remain part of the boundary. Two mappings may use the same relation while selecting different source and target categories; merging them could create semantics that the user did not request.

Connected components prevent unrelated concepts from being combined merely because they use the same relation. Given:

```text
infarction --treated_by--> aspirin
infarction --treated_by--> clopidogrel
pneumonia  --treated_by--> antibiotics
```

the expected result is:

```text
Bundle 1: infarction AND (aspirin OR clopidogrel)
Bundle 2: pneumonia AND antibiotics
```

It must not become:

```text
(infarction OR pneumonia) AND (aspirin OR clopidogrel OR antibiotics)
```

## Compilation approach

The generic compilation algorithm should be semantics-preserving:

1. Resolve Concept Graphs concept IDs to TOP entity or draft IDs.
2. Discard relation edges whose endpoints cannot be resolved.
3. Partition edges according to the bundle boundary above.
4. Compile every edge with the existing `QueryExpansionExpressionCompiler`.
5. Combine the compiled edge expressions with `OR`.
6. Normalize the combined expression by flattening operators, removing duplicates, and applying safe factoring rules.

For a required-context relation, the initial combined form is:

```text
OR(
  AND(infarction, aspirin),
  AND(infarction, clopidogrel)
)
```

A safe normalization can factor the common source:

```text
AND(
  infarction,
  OR(aspirin, clopidogrel)
)
```

Compiling edges before combining them avoids inventing separate bundle semantics for every relation strategy.

### Expected forms by strategy

| Strategy | Typical normalized bundle |
| --- | --- |
| `alternatives` | `OR(source, target1, target2)` |
| `optional_context` | `OR(source, target1, target2)` |
| `recall_expansion` | `OR(source, target1, target2)` |
| `require_context` | `AND(source, OR(target1, target2))` |
| `specificity_focus` | `AND(source, OR(target1, target2))` |
| `exclude_context` | `AND(source, NOT(OR(target1, target2)))` |
| `phrase_context` | `OR(XProd(source, target1), XProd(source, target2))` |
| `proximity_context` | `OR(Dist(XProd(source, target1), d), Dist(XProd(source, target2), d))` |
| `ignore` | no bundle |

Normalization must be strategy-safe. In particular, exclusions must not be transformed into an expression such as `OR(NOT(target1), NOT(target2))`, which has different Boolean semantics.

For phrase and proximity strategies, source-target pairs should remain intact unless an equivalence-preserving transformation is established.

## Responsibility split

### `top-document-query`

`top-document-query` should own semantic grouping and expression construction. A possible entry point is a new `QueryExpansionBundleCompiler`.

Possible internal value types:

```java
record QueryExpansionRelationEdge(
    String mappingId,
    String relationId,
    QueryExpansionStrategy strategy,
    String sourceEntityId,
    String targetEntityId) {}

record QueryExpansionBundle(
    String mappingId,
    String relationId,
    QueryExpansionStrategy strategy,
    Set<String> sourceEntityIds,
    Set<String> targetEntityIds,
    Expression expression) {}
```

The exact types remain an implementation decision. The bundle result should retain enough provenance to support presentation, deterministic ordering, diagnostics, and later confidence-based ranking.

Responsibilities include:

- grouping resolved relation edges;
- finding connected components;
- invoking the existing per-edge expression compiler;
- combining expressions;
- normalizing expressions safely;
- returning the contributing entity IDs and edges with each expression.

The compiler should not depend on backend API request or response classes.

### `top-backend`

The backend should continue to own API-specific orchestration and presentation:

- translate the Concept Graphs response into resolved relation edges;
- retain the selected mapping identity during translation;
- invoke the bundle compiler;
- create `CompositeConcept` drafts from compiled bundles;
- create localized titles and descriptions;
- attach the appropriate existing source concept or hierarchy metadata;
- report or log omitted unresolved relations.

## Titles, descriptions, and frontend grouping

The current arrow-style title is suitable for a single edge but not for a bundle:

```text
infarction --treated by--> aspirin
```

A bundle could instead use:

```text
Title:       Infarction — treatments
Description: Infarction treated by aspirin or clopidogrel
```

For a large bundle, the title can summarize the number of alternatives while the description contains more detail:

```text
Title:       Infarction — treatments (4 alternatives)
Description: Infarction treated by aspirin, clopidogrel, heparin, or warfarin
```

The frontend currently derives relation groups by parsing the `--relation-->` title syntax. Bundle implementation should remove this presentation coupling. Prefer explicit relation or bundle metadata in the generated-draft API. If an API change is deferred, a temporary compatibility mechanism should be documented and kept separate from the human-readable title.

## Confidence and evidence

Confidence filtering is related but should not be hidden inside expression compilation. For the initial implementation:

- preserve every valid returned edge;
- do not introduce a new confidence threshold;
- order members deterministically, using confidence and then display title when confidence is available;
- retain contributing edge information in the internal bundle result.

A later iteration can add explicit ranking or filtering policy without redesigning bundle compilation.

## Invalid and incomplete relations

Unresolved endpoints must be handled conservatively:

- use the original source concept only when the response explicitly identifies it as the relation source;
- otherwise skip an edge with an unresolved source or target;
- do not silently attach an unrelated target to the original concept;
- expose the number of skipped edges through logging or diagnostics.

## Suggested implementation phases

### Phase 1: bundle compiler

1. Add relation-edge and bundle value types in `top-document-query`.
2. Implement grouping by mapping, relation, and connected component.
3. Reuse `QueryExpansionExpressionCompiler` for individual edges.
4. Combine compiled expressions without advanced normalization.
5. Add deterministic ordering and focused unit tests.

This phase can initially return the semantics-preserving `OR` of compiled edge expressions.

### Phase 2: expression normalization

1. Flatten nested associative `AND` and `OR` expressions.
2. Remove duplicate operands.
3. Factor identical sources where equivalence is guaranteed.
4. Add specific, tested handling for exclusion expressions.
5. Keep phrase and proximity pairs intact.

### Phase 3: integration and presentation

1. Replace per-edge draft construction in `top-backend` with bundle compilation.
2. Generate bundle-oriented titles and descriptions.
3. Remove frontend parsing of arrow-style titles.
4. Expose explicit bundle grouping metadata if the API model permits it.
5. Add backend and frontend integration tests.

### Later extensions

- configurable confidence filtering;
- maximum bundle size and overflow behavior;
- bundle ranking;
- richer provenance shown in the UI;
- intentional combinations across different relation types.

Cross-relation bundles should not be part of the initial implementation because they require stronger domain-specific semantics and can easily create misleading queries.

## Test cases

The implementation should cover at least:

- multiple targets sharing one source produce one bundle;
- multiple sources sharing one target remain semantically correct;
- disconnected subgraphs produce separate bundles;
- separate selected mappings do not merge;
- separate relation IDs do not merge;
- duplicate edges do not duplicate operands;
- a single edge still produces a valid composite;
- `ignore` edges produce no bundle;
- unresolved endpoints are skipped;
- exclusion grouping preserves Boolean semantics;
- phrase and proximity source-target pairs remain intact;
- bundle and operand ordering is deterministic;
- no returned relations produce no composite drafts.

## Open decisions

Before implementation, confirm:

1. whether connected components are the desired bundle granularity in all initial profiles;
2. whether explicit bundle metadata can be added to the generated-draft API now or should follow later;
3. how many member terms should appear in titles and descriptions;
4. whether normalization belongs in a query-expansion-specific component or a reusable TOP expression utility;
5. whether skipped-edge diagnostics need to be returned to the caller or only logged.
