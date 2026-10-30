# Cross-project index-selection impact

Read [handoff/status](index.md) first. This is a source-location/task inventory, **not implementation
authorization**. Document-query changes exist; sibling-project findings below describe the inspected,
unmigrated source and must be rechecked before editing. Removal tracking: [L01–L12](legacy-removal-inventory.md).

## Scope and key distinction

Java/UI projects are siblings under `IdeaProjects`: `top-document-query`, `top-backend`,
`top-frontend`, `top-api`. Python is under `PycharmProjects/concept-graphs`.

`source.indices` lists allowed Elasticsearch document inputs/destinations. It is **not** a list of
Marqo embedding/RAG indices, Neo4j corpus IDs, or pipeline IDs. Source selection belongs to each
operation; downstream document links should inherit it rather than repeatedly prompt the user.

## Operation inventory

| Operation | Needed choice/context; pending unless noted |
| --- | --- |
| Browse/name filter/page | Source-index selector, potentially multi-select; preserve scope through filters/pages. Library supports scoped reads. |
| Upload/import | One explicit destination; creation/check/bulk-write now use it in the library. UI/backend still need transport and scoped polling. |
| Create/execute concept query | Select and persist indices. Library execution can be scoped; query model/storage cannot yet carry that choice. |
| Saved-query results | Restore stored scope and each hit's index; never reinterpret existing results using a new choice. Backend ID-keyed maps need replacement. |
| Start pipeline from Elasticsearch | Initially propose one input index, through both JSON and multipart starts; persist source/index binding. Backend currently forwards the first configured index. |
| Start pipeline from ZIP | No Elasticsearch input selection needed; distinguish uploaded versus server input. |
| Resume/restart pipeline | Reuse stored binding; changing input must not reuse incompatible artifacts silently. |
| Publish clusters to Neo4j | Read original documents using pipeline binding; no independent Elasticsearch write destination. |
| Details/previous-next/highlights | Inherit exact `(dataSourceId, index, documentId)` plus graph corpus context where relevant. |
| Phrase/cluster-filtered browsing | Resolve graph-derived IDs only against their original input binding. |
| RAG initialization/status/questions | Select/inherit process, not arbitrary document-source or internal Marqo indices. |
| RAG filters/source links | Resolve process to source/index; current frontend often assumes process equals data-source ID. |
| Counts | No selector for an intentional global total; explicitly scope library counts and define aggregation/deduplication. |
| Python document-server check | Caller supplies selected input index; Python already checks that index. |
| Standalone Streamlit pipeline UI | Already has free-text “Index / collection”; a selectbox needs a source of configured choices. |

Single/multi-select UX, automatic single-option selection, and pipeline naming remain proposals, not
implemented cross-project policy. Duplicate document IDs across indices require provenance, not just selectors.

## Document-query: implemented boundary

Under `src/main/java/care/smith/top/top_document_query/`:

| Source | Present responsibility |
| --- | --- |
| `adapter/config/AdapterConfigLoader.java`, `TextAdapterConfig.java`, `Connection.java` | V1/v2 normalization/validation; complete ordered Elasticsearch and service endpoint lists. Legacy getters remain; full-list service fallback is not wired downstream yet. |
| `adapter/IndexSelection.java`, `TextAdapter.java` | Validated immutable scopes and adapter entry points. `TextFinder.java` delegates to the supplied adapter; caller must provide the intended scope. |
| `adapter/elasticsearch/ElasticsearchAdapter.java` | Scoped reads/imports, exact references, indexed reads, PIT batching, startup fallback/client ownership. |
| `adapter/DocumentReference.java`, `IndexedDocument.java`, `DocumentHit.java` | Exact identities and provenance-bearing results. |
| `converter/csv/CSVDataRecord.java`, `DocumentCSV.java` | Appended index/data-source fields, exact-reference reading, retained legacy column APIs. |

Also changed: `src/main/resources/text_adapter_config_schema.json`,
`src/test/resources/config/Example_Adapter_v2.yml`, and the configuration guide at
`src/test/resources/config/README.md`. Tests: `DocumentHitTest`, `DocumentReferenceTest`,
`ElasticsearchIndexScopeTest`, `IndexSelectionTest`, `SchemaValidationTest`, `TextAdapterConfigTest`.
Verification/review state is maintained only in [the handoff](index.md).

Remaining integration boundaries: shared `Document`/query models and stored result maps belong to
other projects; typed public nested Java configuration and missing indexed filter equivalents are
future decisions, not already implemented replacements. See the legacy inventory before cleanup.

## Backend: pending selection transport and binding

Paths under `top-backend/src/main/java/care/smith/top/backend/`:

| Source | Required work |
| --- | --- |
| `api/nlp/DocumentApiDelegateImpl.java` | Accept/validate read scope, one import destination, exact document references; preserve all filter branches. |
| `service/nlp/DocumentService.java` | Scope adapters safely; review `dataSourceAdapter`/`queryAdapter` cache keys and global/scoped counts. |
| `service/nlp/DocumentQueryService.java` | Persist/apply query scope; replace ID-only result reconstruction; expose authorized index choices. Current `textAdapterConfigToDataSource()` returns no indices. |
| `api/nlp/ConceptPipelineApiDelegateImpl.java` | Replace first-index choice in `createDocumentServerConfigMap()`; pass selection through JSON/multipart; separate pipeline name from source lookup. Forward full ordered service/Elasticsearch endpoint lists instead of split URL/port projections. |
| `service/nlp/ConceptGraphsService.java` | Retain process-to-input binding. Adapter-specific endpoint selection currently differs between JSON and multipart starts. |
| `service/nlp/ConceptClusterService.java` | Hydrate graph documents from the pipeline-bound input in `createSpecificGraphsInNeo4j()` / `createGraphInNeo4j()`. |
| `api/nlp/ConceptClusterApiDelegateImpl.java`, `PhraseApiDelegateImpl.java` | Exact original-document lookup and correct graph-corpus binding. |

Enforce source access and configured index allowlists server-side, including discovery. Never mutate
shared/cached configuration to select a request's index. A configured import destination may not exist
yet; reads/ingestion should report missing or empty inputs instead of silently switching indices.

## Frontend: pending controls and inherited context

Paths under `top-frontend/src/`:

- `pages/DocumentSearch.vue`: add browsing selection beside data source; query-result mode restores scope.
- `components/Documents/DocumentSearchForm.vue`: scope `reloadDocuments()` / `filterDocuments()`;
  choose one upload target and scope `checkForUpload()`; pass references via `chooseDocument()`;
  preserve scope in `routeToDocumentQuery()` / `showRegenerateDialog()`. Pipeline/cluster/RAG handling
  currently derives process/corpus from `dataSource.id`.
- `components/Query/ConceptQueryForm.vue`, `pages/QueryBuilder.vue`: query selection, validation,
  prefill/reset/import/export and routing.
- `components/Documents/ConceptClusterDialog.vue`: server-input selection in JSON/multipart starts;
  stored resume binding. Current start uses source ID as pipeline ID too.
- `components/Documents/DocumentDetailsDialog.vue`: carry exact reference through lookup/navigation.
- `components/Documents/RAGQuestionDialog.vue`: stop passing `process` as the original data-source
  argument to `getSingleDocumentById()`; resolve actual source/index.
- `stores/entity-store.ts`: typed choice discovery; reset incompatible selections on source change.
  Add labels/translations with the eventual controls.

## API: pending authoritative contracts

Source: `top-api/schemas/top-api.yml`; change schemas and regenerate consumers, not generated code.

- `DataSource` or dedicated authorized discovery: available index names, not credentials/full config.
- `ConceptQuery`: persisted scope; result IDs/offsets and document responses: index-aware identity.
- `getDocuments`, `importDocuments`, `getSingleDocumentById`: scope/destination/exact reference.
- `getDocumentsForQuery`: inherit stored query scope.
- `getPhrasesByDocumentId`, `getConceptClusterByDocumentId`: original index plus graph-corpus binding.
- `ConceptPipelineRequest`: selected server input for multipart; document/validate the JSON string
  request separately. Existing `ElasticsearchServerConfig.index` denotes one chosen input, not an allowlist.
- RAG source references: original identity and process-to-input binding.

## Concept-graphs: selected input versus internal storage

Paths under `concept-graphs/`:

- `src/api/pipeline_support/request_data.py`: JSON/multipart parsing.
- `src/api/pipeline_support/document_server.py`: forwards one configured input index.
- `src/api/services/document_server.py`: parses input index; uses `/{index}/_count`, `/{index}/_search`, scroll.
- `src/api/routes/status.py`: checks that input index.
- `src/gui/app.py`: explicit “Index / collection” input.

**Single-index ingestion already accepts an explicit index.** Initial selector transport belongs in
TOP's UI/API/backend. Python does not need the full adapter allowlist just to execute that choice.
Persist input provenance for resume and document links; its representation remains undecided.

Do not conflate this with `src/core/embedding_functions.py`, `src/pipeline/steps/embedding_util.py`,
`src/storage/marqo/embedding_store.py`, or `src/pipeline/document_addition.py`: their index/cache
settings refer to vector storage. `src/api/services/rag_vectorstore.py` creates `<process>_rag`.
Query expansion is not itself a document-source read; subsequent query execution needs scope.

## Suggested sequence, not authorization

Agree API/UX/pipeline identity; migrate contracts and backend; add frontend controls; persist input
binding and make resume/graph/RAG resolution safe. Then test duplicate IDs, differing concurrent
scopes, destination creation, JSON/multipart consistency, query reload/export, resume mismatch and
source links. Compatibility removal is a separate gated task.

## Knowledge consulted in the original review

`concept-graphs/okf/concept-graphs-architecture/`: `index.md`, `modules/api.md`,
`modules/storage.md`, `modules/rag.md`. Findings were checked against source. No concept-graphs
behavior was changed, so no OKF update was needed.
