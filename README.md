# top-document-query

`top-document-query` contains document-query adapters used by TOP, especially the Elasticsearch text adapter and integrations with external [Concept Graphs services](https://github.com/Onto-Med/concept-graphs).

## Text adapter configuration

Document data sources are configured via YAML files and parsed into `TextAdapterConfig`.

Example:

```yaml
id: Example_Data_Source
adapter: care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter
connection:
  url: http://localhost
  port: 9200
index:
  - documents
field:
  - text
batchSize: 30
replaceFields: {'text': 'content'}
labelKey: label
```

## Concept Graphs connection

Adapters can reference a [Concept Graphs service](https://github.com/Onto-Med/concept-graphs):

```yaml
conceptGraph:
  connection:
    url: http://localhost
    port: 9007
```

This connection is used by TOP backend services that call [Concept Graphs](https://github.com/Onto-Med/concept-graphs) functionality such as concept pipelines, RAG, and query expansion.

## Query expansion policy

A data source can define how semantic query-expansion relations should be translated into TOP query-building intent.

```yaml
queryExpansion:
  profile: medical_de
  relations:
    equivalent_to:
      strategy: alternatives
    related_to:
      strategy: optional_context
    may_indicate:
      strategy: require_context
    treated_by:
      strategy: require_context
    investigated_by:
      strategy: require_context
    confirmed_by:
      strategy: require_context
    broader_than:
      strategy: recall_expansion
    narrower_than:
      strategy: specificity_focus
```

`profile` references a [Concept Graphs](https://github.com/Onto-Med/concept-graphs) query-expansion profile. Relations exposed to the frontend should be the intersection of:

1. relations configured in this data source, and
2. relations allowed by the referenced [Concept Graphs](https://github.com/Onto-Med/concept-graphs) profile.

The strategy names are domain-neutral retrieval intents. They should not expose concrete Boolean operators to users.

Initial strategy vocabulary:

- `alternatives`
- `optional_context`
- `require_context`
- `exclude_context`
- `recall_expansion`
- `specificity_focus`
- `proximity_context`
- `phrase_context`
- `ignore`

Available default strategies are defined centrally in `QueryExpansionStrategy`. Backend services can expose a selected adapter's `TextAdapter.getQueryExpansionStrategyDefinitions()` to the frontend, similar to how TOP expression functions are exposed via `SONG.getExpressionFunctions()`.

The current default TOP compiler maps supported strategies to existing expressions such as `OR`, `AND`, or `NOT` in `QueryExpansionExpressionCompiler`. Adapters can override `TextAdapter.compileQueryExpansionRelation(...)` and `TextAdapter.getQueryExpansionStrategyDefinitions()` if they support more specific behavior. This mapping is intentionally hidden from domain users and should not be duplicated in the frontend.

Initial expression mapping:

- `alternatives` → `OR(source, target)`
- `optional_context` → `OR(source, target)`
- `require_context` → `AND(source, target)`
- `exclude_context` → `AND(source, NOT(target))`
- `recall_expansion` → `OR(source, target)`
- `specificity_focus` → `AND(source, target)`
- `ignore` → no expression

Additional generic text-expression mappings:

- `proximity_context` → `Dist(XProd(source, target), 5)`
- `phrase_context` → `XProd(source, target)`

The proximity distance is currently a default compiler constant and can become configurable later if needed. Runtime behavior still depends on the selected adapter's support for the generated TOP text functions.

## Authentication

Authentication details can be overwritten on adapter level, but are declared first at framework level.
