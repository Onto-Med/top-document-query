# Start here: v2 configuration and index-selection handoff

**Review state:** document-query implementation exists, but the user is still reviewing changes.
Implemented does **not** mean individually reviewed/accepted or integrated end-to-end.

## Read next

- [Cross-project impact](index-selection-impact.md): implementation boundary, source locations, pending work.
- [Legacy-removal inventory](legacy-removal-inventory.md): compatibility items L01–L11 and removal gates.
- [Configuration/usage guide](../src/test/resources/config/README.md) and
  [v2 example](../src/test/resources/config/Example_Adapter_v2.yml).

This file owns decision/review status and verification history. The other inventories own technical
locations and removal prerequisites. Prefer current source over stale prose; update both when they diverge.

## Agreed decisions

- Configure available document indices only under `source.indices`; no `readIndices`/`writeIndex` split.
  Choose the target in the operation/UI instead of relying on list order.
- Keep one shared `source.batchSize` for Java retrieval/page sizes and Python ingestion fetches.
- Use ordered complete URI lists: Elasticsearch `connection.endpoints` and service
  `services.conceptGraph.endpoints` / `services.vectorStore.endpoints` (primary, then fallbacks).
- Preserve downstream ingestion mappings and concept-graph/vector-store settings: they have consumers.
- Keep the implemented `SearchCursor` pagination redesign after explicit user review: PIT snapshot,
  full server sort values, `_shard_doc` tie-breaker, and cursor cleanup. Do not revert it speculatively.
- Track retained compatibility before removing it. The compatibility rationale was explained;
  **no legacy removal or blanket deprecation has been authorized**.

## Implemented here, still under broader review

| Area | Present implementation |
| --- | --- |
| YAML/schema | V1 and v2 loading, validation, duplicate/unknown/mixed-key rejection, v2 example. |
| Java configuration | V2 normalizes into existing flat getters; a public nested Java model does not exist yet. |
| Index scope | `forIndex()` / `forIndices()` return independent views sharing the client, without changing the allowlist. V2 multi-index reads require explicit scope. |
| Import/lookup | Single selected import destination; ID-only lookup requires one index; exact-reference lookup supports configured aliases. |
| Provenance | `DocumentReference`, `IndexedDocument`, indexed reads, and query-hit/CSV source/index fields. Old plain-document APIs remain. |
| Connections | Elasticsearch: complete URIs/HTTPS, ordered startup fallback, failed-transport cleanup, ownership-aware `close()`. Services: ordered lists loaded/validated; full-list runtime fallback still needs backend/Python integration. |
| Batching | PIT-backed `SearchCursor`, shared by query execution and document streams. |

Additional behavior changes requiring attention during review: query/stream failures now propagate
rather than look like empty final batches; invalid config throws instead of returning null; all failed
connection attempts throw; stream callers must close early-stopped streams. Endpoint paths/credentials
are rejected, not silently discarded. These changes must not be presented as behavior-neutral.

The implementation and its tests were changed **only in this repository**. Sibling-project review was
read-only. No claim is made that existing backend/frontend flows already support multi-index selection.

## Pending decisions and authorization boundary

Still to decide: UI single/multi-select policy, shared API document/query models, pipeline identity and
persisted source binding, legacy-request/archive policy, and eventual public Java configuration shape.
Library support for a feature is not approval of the corresponding end-to-end UX/contract.

Next expected activity is answering the user's remaining implementation-review questions. The task
lists in these docs are **not instructions to automatically implement sibling-project changes, remove
compatibility APIs, or broaden the refactor**. Follow the user's next explicit request.

## Recorded verification

- Offline Maven `test-compile` succeeded, targeting Java 21 with a local JDK 24.
- Latest service-endpoint change: offline `test-compile` and 30 targeted tests passed via a temporary
  cached-JUnit launcher under ignored `target/` (previous checkpoint: 27 tests):
  `TextAdapterConfigTest`, `IndexSelectionTest`, `ElasticsearchIndexScopeTest`,
  `DocumentReferenceTest`, `DocumentHitTest`, `SchemaValidationTest`.
- Standard Maven test execution was blocked by an uncached Surefire JUnit provider. The temporary
  launcher is not a portable committed test command; normal Maven execution remains unverified here.
- No Docker integration tests, downloads, or external service calls were performed.
- `git diff --check` passed at the implementation checkpoint; rerun after subsequent edits.

For subsequent runs, use the targeted command in the configuration guide; request approval before
network downloads or expensive/container suites. Do not assume ignored verification helpers still exist.

## Keep the handoff current

- Read `git status` and relevant source before editing; do not overwrite user changes or assume commits.
- Update decision/review status here only when the user actually agrees or a new check is run.
- Update cross-project task status when that project's code changes; recheck its callers before claiming completion.
- Resolve/retire legacy IDs only after their removal gates are met; retain historical-data considerations.
- Keep detailed usage in the configuration guide, not duplicated throughout `docs/`.
