# Legacy and compatibility removal inventory

Read [handoff/status](index.md) first. **All L01–L11 are open; tracking is not deprecation or deletion
authorization.** Scope: compatibility retained during this migration, not a full historical dead-code audit.
Callers below were source-checked locally/in backend; recheck external consumers before removal.

Locations are relative to `src/main/java/care/smith/top/top_document_query/`, unless noted.
Backend paths are relative to `../top-backend/src/main/java/care/smith/top/backend/`.
See [cross-project work](index-selection-impact.md) and [usage](../src/test/resources/config/README.md).
No local knowledge bundle was found; this inventory is source-based.

## L01 — Document-only listing/filtering APIs

Locations: `adapter/TextAdapter.java`, `adapter/elasticsearch/ElasticsearchAdapter.java`.
These methods respect selected indices but omit provenance in returned `Document` objects.

| Retained API | Indexed replacement |
| --- | --- |
| `getAllDocumentsPaged/Batched(...)` | `getAllIndexedDocumentsPaged/Batched(...)` |
| `getDocumentsByIdsPaged/Batched(...)` | `getIndexedDocumentsByIdsPaged/Batched(...)` |
| `getDocumentsByNamePaged(...)` | `getIndexedDocumentsByNamePaged(...)` |
| `getDocumentsByNameBatched(...)` | No dedicated adapter-neutral equivalent yet. |
| Both `getDocumentsByTerms(...)` overloads | No matching adapter-neutral equivalent yet. |
| Both `getDocumentsByIdsAndTerms(...)` overloads | No matching adapter-neutral equivalent yet. |

Elasticsearch's `searchIndexedDocuments(Query, ...)` / `streamIndexedDocuments(Query, ...)` cover
advanced queries, but are not replacements for every abstract adapter contract.

Callers: backend `api/nlp/DocumentApiDelegateImpl.java` (browse/filter/query results),
`service/nlp/ConceptClusterService.java` (`createGraphInNeo4j()` hydration);
local `ElasticsearchAdapterTest` and `ElasticsearchIndexScopeTest`.

**Gate:** migrate consumers/response models and provide missing filter equivalents, preserving
AND/OR/default and combined ID/term semantics. Do not lose capabilities during removal.

## L02 — ID-only single-document lookup

Locations: `TextAdapter.getDocumentById(...)`, Elasticsearch override.
Replacement: `getDocument(DocumentReference, ...)` with source/index/Elasticsearch ID.

Callers: backend `DocumentApiDelegateImpl`, `PhraseApiDelegateImpl`, `ConceptClusterApiDelegateImpl`
under `api/nlp/`, plus local tests. **The base exact-reference method also delegates through the old
ID-only hook**; Elasticsearch's override does not.

**Gate:** migrate exact references through details/highlights/phrase/cluster requests and replace
that base delegation/other adapter implementations. Test duplicate IDs and aliases. Returning plain
`Document` after an exact-reference lookup is not itself a legacy problem.

## L03 — Document-only conversion helpers

Location: `adapter/elasticsearch/ElasticsearchAdapter.java`:
`documentStream(...)`, `toPage(...)`, `documentCollector(...)`.

These project indexed/hit results back to plain documents. **Gate:** reassess after L01/L02, not
independently. `documentCollector()` also serves the exact-reference lookup and cannot just be deleted.

## L04 — Import overload without destination argument

Locations: `TextAdapter.importDocuments(Document[], String)` and Elasticsearch override.
Explicit entry: `importDocuments(String index, Document[], String)`.

Caller: backend `api/nlp/DocumentApiDelegateImpl.importDocuments()`.
Local `ElasticsearchAdapterTest.createIndexAndUploadDocuments()` now uses an explicit destination;
scope tests still intentionally exercise legacy dispatch/rejection.

The older signature is still the **scoped implementation hook**: the new overload calls
`forIndex(index)` then that hook. Direct use on a single-index-scoped adapter is unambiguous.

**Gate:** decide whether to keep scoped imports; if removing this signature, redesign dispatch and
migrate callers first. The old first-configured-index write fallback is already gone; do not restore it.

## L05 — V1 YAML and implicit version

Locations: `adapter/config/AdapterConfigLoader.load()` (missing version means 1),
`TextAdapterConfig.configVersion = 1`, legacy definitions/alternative in
`src/main/resources/text_adapter_config_schema.json`, and
`src/test/resources/config/Example_Adapter.yml` / `Example_Adapter_Local.yml`.

Legacy layout: `index`, `field`, `batchSize`, `replaceFields`, `labelKey`, `otherId`, `dateField`,
`conceptGraph.connection`, `vectorStore.connection`, split URL/port/alternate URL.
Replacement: v2 layout, not deletion of the underlying settings.

**Gate:** migrate deployed files/tests and programmatic defaults; intentionally reject missing/v1
versions and remove the schema branch together. Keep schema and loader behavior consistent.

## L06 — Flat Java configuration / v2 normalization

Locations: `adapter/config/TextAdapterConfig.java`, `AdapterConfigLoader.fromV2()`.
The fields below and their getter/setter pairs are still the **active Java model for v2**, not unused aliases.

| Java attribute | V2 input |
| --- | --- |
| `index` | `source.indices` |
| `field` | `search.fields` |
| `batchSize` | `source.batchSize` |
| `replaceFields` | `pipeline.documentMapping.renameFields` |
| `labelKey` | `pipeline.documentMapping.labelField` |
| `otherId` | `pipeline.documentMapping.idField` |
| `conceptGraph`, `vectorStore` | Corresponding entries under `services` |

V2 records are input-only; no public `getSource()/getSearch()/getPipeline()/getServices()` exists.
Local dependencies: loader, `IndexSelection`, Elasticsearch adapter, configuration/integration tests.
Backend: `ConceptPipelineApiDelegateImpl` uses these getters for genuinely consumed downstream settings.
`ConceptGraphConfig` / `VectorStoreConfig` and their `connection` wrappers are part of this bridge.

`getReplaceFieldsAsString()` has no caller found in inspected local/backend NLP sources; it is a
public API removal candidate, **not evidence that field mapping is unused**.

**Gate:** agree/implement a public replacement model, migrate local/downstream access and serialization
assumptions, then remove flat fields/accessors and normalization. Converting YAML alone is insufficient.

## L07 — Split connection addresses

Location: `adapter/config/Connection.java`: `url`, `alternateUrl`, string `port`, their getter/setter
pairs, `legacyEndpoint(...)`, `hasLegacyAddress()`, mixed-format checks; reassess
`baseUrl(...)` / `defaultPort(...)` after removing projections.

Replacement: `getEndpoints()/setEndpoints()`, populated from v2 Elasticsearch and service `endpoints`
lists. Old getters project these to primary URL/port and one alternate URL; this loses further
endpoints and the alternate's independent port. No full-list service fallback is wired downstream yet.

Blockers: backend `ConceptPipelineApiDelegateImpl.createDocumentServerConfigMap()`,
`createVectorStoreServerMap()`, `createCgServerMap()`; local `TextAdapterConfigTest` verifies
projections/legacy input. `AbstractElasticTest.initAdaper()` now replaces the legacy connection with
the container's sole endpoint rather than mutating its port.

**Gate:** migrate backend forwarding, downstream Python URL/port contracts, and tests; preserve
HTTPS, ordered endpoints and independent ports. Keep full-URI validation/handling.

## L08 — V1 selection/normalization and mutable configuration usage

Location: `adapter/IndexSelection.java`: version-1 branches in `select()` / `configured()` normalize
through `NLPUtils.stringConformity()`; `defaultRead()` permits unscoped all-index legacy reads.
Replacement: exact v2 names and explicit multi-index scope.

**Gate:** complete L05 and request-scope migration, then remove version-specific branches, not
`IndexSelection` or the globally used naming utility.

Older code can still use `getConfig().setIndex(...)` as operation selection; migrate any such caller
to a configured allowlist and scoped/explicit import. `ElasticsearchAdapterTest` now follows that
pattern without shared configuration mutation. Configuration construction remains valid; do not
delete every setter.

## L09 — CSV compatibility and optional hit provenance

Locations: `converter/csv/DocumentCSV.java`, `CSVDataRecord.java`, `adapter/DocumentHit.java`.

- `readFirstColumn()/readColumn()` accept old four-column data headers. Backend
  `DocumentQueryService.getDocumentIdsAndOffsets()` uses them to build an ID-keyed map, losing indices.
- Original CSV positions remain; `index`/`dataSource` are appended.
- Four-argument `CSVDataRecord` and both old `DocumentHit` constructors allow absent provenance.
  Elasticsearch fills hit provenance via setters; manual/older callers may omit it.
- Writing incomplete hits emits blank provenance. Exact-reference APIs cannot invent what is missing.

`readDocumentReferences()` reads identities, **not full scored/highlighted/offset records**.
**Gate:** migrate result consumers/constructors and persisted archives; provide reference-aware result
records, then decide whether a separate historical reader must remain. Do not casually break old archives.

Keep IDs, scores, titles, offsets, and potentially generic `readColumn()`; the issue is ID-only identity,
not those attributes themselves. In particular, `DocumentHit.getDocumentId()` is not automatically obsolete.

## L10 — Unused legacy date metadata

Locations: `TextAdapterConfig.dateField`, getter/setter, `toString()`, legacy schema property.
No runtime consumer found in the earlier document-query/backend/concept-graphs source review;
v2 omits it. **Gate:** confirm external/deployed use before removal with/after L05. Do not invent a
replacement or remove unrelated query date-restriction models.

## L11 — New abstract-adapter migration defaults

Location: `adapter/TextAdapter.java`: default `forIndices()` and five indexed-list methods throw
`UnsupportedOperationException` to preserve old subclasses. Exact-reference/import defaults still
use older hooks (L02/L04).

These are new bridges, not obsolete indexed features. **Gate:** inventory/migrate supported subclasses,
then consider required abstract methods or capability interfaces. Do not remove the replacement APIs.

## Removal rules

Migrate callers and historical data, implement missing equivalents, agree deprecations, then remove
in an intentional breaking release. Resolve IDs and update [handoff](index.md) in the same change.
A compiling repository alone does not cover deployed YAML, historical exports or external subclasses.

Keep the agreed `SearchCursor`/PIT design. References, indexed results, scoped views, ownership/close,
client injection, scoped `count()/execute()`, and actual ingestion/service settings are not removal
targets merely because compatibility code surrounds them. Proposed future reshaping is not approval
of every current implementation detail; see the handoff's review boundary.
