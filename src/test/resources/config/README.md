# top-document-query Adapter

## Configuration

Both legacy YAML and `configVersion: 2` are supported by `TextAdapterConfig.getInstance(path)`.
See [`Example_Adapter_v2.yml`](src/test/resources/config/Example_Adapter_v2.yml) and the
[configuration schema](src/main/resources/text_adapter_config_schema.json).

```yaml
configVersion: 2
id: example-documents
connection:
  endpoints:
    - https://localhost:9201
    - http://localhost:9202
source:
  indices: [documents, imported_documents]
  batchSize: 30
search:
  fields: [text]
pipeline:
  documentMapping:
    renameFields: {text: content}
    labelField: label
    idField: id
services:
  conceptGraph:
    endpoint: http://localhost:5000
  vectorStore:
    endpoint: http://localhost:8882
```

- `source.indices` lists allowed index/alias names, not an implicit import destination. V2 requires
  explicit, unique, lowercase names rather than wildcard expressions.
- `source.batchSize` is shared by Java retrieval/page sizes and downstream ingestion fetches.
- `search.fields` controls search/highlight fields. Pipeline mappings are downstream ingestion
  settings; they do not change Java's fixed `DocumentEntity` fields.
- Services are optional in this library. The calling backend must handle missing services explicitly.
- Complete HTTP(S) endpoints preserve scheme and per-endpoint ports. Elasticsearch endpoints are
  tried in order at initialization; failed transports are closed and failure of all endpoints throws.
  This is startup fallback, not automatic per-request failover or a node pool. URI paths, embedded
  credentials, query strings, and fragments are rejected rather than silently discarded.
- HTTPS uses the JVM's default trust configuration. Authentication/custom TLS configuration is not
  introduced here. Authentication details are intended to be inherited from the framework and
  overridden at adapter level, but this library does not yet wire those overrides into the client.
- Defaults: Elasticsearch adapter class, batch size 30, search field `text`, label field `label`,
  ID field `id`, and rename mapping `text -> content`. V2 requires connection endpoints and indices.
- Invalid file configuration throws `IllegalArgumentException`, rather than logging and returning null.
  Unknown/mixed legacy-v2 keys and duplicate YAML keys are rejected.

### Java compatibility

The file loader normalizes v2 into `TextAdapterConfig`'s existing Java getters (`getIndex()`,
`getBatchSize()`, `getField()`, `getReplaceFields()`, etc.). These getters describe configuration,
**not the scope of an individual request**. Use the file loader for v2 YAML rather than directly
binding it to `TextAdapterConfig` with an unrelated `ObjectMapper`. Serializing the normalized Java
object does not produce the original v2 YAML layout.

`Connection.getEndpoints()` is authoritative. Legacy `getUrl()` / `getPort()` /
`getAlternateUrl()` are retained as a migration bridge. They cannot express three or more endpoints
or an alternate's independent port. Downstream endpoint-forwarding code must migrate to complete
URIs before relying on these features end-to-end.

Legacy files retain all-configured-indices read behavior and legacy index-name normalization.
Legacy `dateField` is retained as metadata but is not used by the adapter; v2 does not expose it.

## Request-scoped indices

```java
TextAdapterConfig config = TextAdapterConfig.getInstance("adapter.yml");
try (ElasticsearchAdapter adapter = new ElasticsearchAdapter(config)) {
  ElasticsearchAdapter documents = adapter.forIndex("documents");
  var page = documents.getAllIndexedDocumentsPaged(0, true);

  // Explicit import destination, independent of the configured list order.
  adapter.importDocuments("imported_documents", uploads, "en");

  try (var batches = adapter.forIndices(List.of("documents", "imported_documents"))
      .getAllIndexedDocumentsBatched(30, false)) {
    batches.forEach(batch -> batch.forEach(result -> {
      DocumentReference reference = result.reference();
      // reference contains dataSourceId, concrete hit index, and Elasticsearch document ID.
    }));
  }
}
```

- `forIndex()` / `forIndices()` return independent scoped views sharing the client's transport.
  They do not mutate the configured allowlist. A view can narrow, but not expand, its current scope.
- An unscoped v2 adapter auto-selects a sole configured index; multiple indices require selection.
  To count all configured indices intentionally, explicitly scope to the full list first.
- Import requires exactly one selected index, including for legacy multi-index configurations.
  The previous implicit first-index import destination is intentionally removed. Existence checks,
  index creation, and bulk writes all use the selected destination.
- `getDocumentById()` requires one selected index and rejects multiple matches through an alias.
  `getDocument(DocumentReference, simplified)` resolves an exact reference through the allowed
  scope, including a concrete index returned by a configured alias.
- `getAllIndexedDocumentsPaged/Batched`, `getIndexedDocumentsByIdsPaged/Batched`, and
  `getIndexedDocumentsByNamePaged` preserve provenance. Elasticsearch also exposes
  `searchIndexedDocuments(Query, page, simplified)` and `streamIndexedDocuments(Query, batchSize,
  simplified)` for advanced filters. Existing `Document`-only methods remain but discard provenance;
  do not use ID-only results to identify documents across multiple indices.
- Batched reads use a point-in-time snapshot with a `_shard_doc` tie-breaker and the server's complete
  sort values, avoiding name collisions during multi-index pagination. `name.keyword` must remain
  sortable in the source indices. Streams close their PIT on exhaustion or request failure; **close
  streams explicitly when stopping early**. Abandoned PITs expire after the one-minute keep-alive.
- Close the owning adapter after its scoped views and streams are finished. Closing a view does not
  close the shared transport. A caller-supplied `ElasticsearchClient` remains caller-owned.

## Query results and CSV

`DocumentHit` now retains the concrete Elasticsearch index and data-source ID and exposes
`getReference()`. `DocumentReference` identifies a document by `(dataSourceId, index, documentId)`.

New CSV exports append `index` and `dataSource` columns after the existing `id`, `score`, `title`,
and `offsets` columns. Existing positions and legacy column-reading methods remain compatible.
`DocumentCSV.readDocumentReferences()` reads exact identities without collapsing identical IDs
from different indices; it rejects legacy exports lacking provenance instead of guessing it.

Existing backend ID-keyed result maps, persisted queries, shared TOP API models, frontend selectors,
and pipeline-to-source bindings still need changes in their owning projects. See the
[cross-project impact inventory](docs/index-selection-impact.md).

## Validation and tests

The schema accepts both versions and rejects unknown/mixed keys. File loading also validates required
values, positive batch sizes, endpoints, and allowed v2 names. Programmatic callers can invoke
`config.validate()`; the Elasticsearch adapter does so before constructing a client.

Targeted non-container tests:

```sh
mvn -o -Dtest=TextAdapterConfigTest,IndexSelectionTest,ElasticsearchIndexScopeTest,DocumentReferenceTest,DocumentHitTest,SchemaValidationTest test
```

`ElasticsearchIndexScopeTest` records client requests with an in-memory transport; it opens no sockets.
The separate existing Elasticsearch integration tests require Docker and are not part of that command.
