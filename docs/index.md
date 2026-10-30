# Start here: v2 configuration and index-selection handoff

**Review/verification state:** file-by-file user review is complete. The user reports all existing
CI checks passing after version alignment and the search fix. Broader cross-project readiness and
additional edge-case integration coverage are not established by that result.

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
  **no blanket legacy removal/deprecation is authorized**. The PIT shim (L12) was specifically
  approved for removal after aligning the target server and clients to 8.19.23.

## Implemented and user-reviewed here

| Area | Present implementation |
| --- | --- |
| YAML/schema | V1 and v2 loading, validation, duplicate/unknown/mixed-key rejection, v2 example. |
| Java configuration | V2 normalizes into existing flat getters; a public nested Java model does not exist yet. |
| Index scope | `forIndex()` / `forIndices()` return independent views sharing the client, without changing the allowlist. V2 multi-index reads require explicit scope. |
| Import/lookup | Single selected import destination; ID-only lookup requires one index; exact-reference lookup supports configured aliases. |
| Provenance | `DocumentReference`, `IndexedDocument`, indexed reads, and query-hit/CSV source/index fields. Old plain-document APIs remain. |
| Connections | Elasticsearch: complete URIs/HTTPS, ordered startup fallback, failed-transport cleanup, ownership-aware `close()`. Services: ordered lists loaded/validated; full-list runtime fallback still needs backend/Python integration. |
| Batching | PIT-backed `SearchCursor`, shared by query execution and document streams. |

Behavior changes to preserve explicitly in validation: query/stream failures now propagate
rather than look like empty final batches; invalid config throws instead of returning null; all failed
connection attempts throw; stream callers must close early-stopped streams. Endpoint paths/credentials
are rejected, not silently discarded. These changes must not be presented as behavior-neutral.

The implementation and its tests were changed **only in this repository**. Sibling-project review was
read-only. No claim is made that existing backend/frontend flows already support multi-index selection.

## Pending decisions and authorization boundary

Still to decide: UI single/multi-select policy, shared API document/query models, pipeline identity and
persisted source binding, legacy-request/archive policy, and eventual public Java configuration shape.
Library support for a feature is not approval of the corresponding end-to-end UX/contract.

Baseline review/testing is complete: local non-container tests pass and the user reports a clean
CI run, including the existing Elasticsearch integration suite. Local Docker remains unavailable;
CI provides that execution environment. Await the next agreed task. These lists are **not instructions
to automatically implement sibling-project changes, remove compatibility APIs, or broaden the refactor**.

## Recorded verification

- Latest CI result (2026-10-30): user reports the workflow completed without problems/errors after
  the `search_after` fix, with clients/test server aligned to 8.19.23 and the PIT shim removed.
  This covers the existing workflow suites; the agent did not fetch the run or verify its totals.
- Latest local check (2026-10-30): the CI non-container selection passed offline through standard
  Maven: **43 tests, 0 failures/errors/skips**, including 12 existing `SongTest` cases and a new
  standard-client PIT regression. The serialized-search regression reproduced the empty
  `search_after` bug before the conditional fix and passed afterward. Compilation targets Java 21; the local check used JDK 24, while CI uses JDK 21.
- Earlier migration-focused selection: 30 tests passed through both the cached launcher and Maven.
  Approved Central downloads resolved the Surefire provider blocker; the temporary launcher is no
  longer required. The configuration guide retains the narrower targeted command.
- Current target setup assumes deployment on Elasticsearch 8.19.23: both Java clients and the test
  image now match it. Testcontainers modules use the 2.0.3 BOM; the shared core's exclusions are
  centralized in `dependencyManagement`, with one `testcontainers.version` property. The 43-test
  local check includes these changes; dependency-tree inspection is blocked by an uncached plugin.
- Historical user-supplied CI log (2026-10-29): **12 integration tests, 5 errors, 0 assertion failures/skips**.
  All five errors were PIT opening: Elasticsearch 8.8.1 rejected the empty body sent by the 8.18.8
  client. A bodyless/id-only compatibility shim was introduced, then removed with user approval on
  2026-10-30 after aligning clients and the target/test server to 8.19.23. `SearchCursor` now uses
  standard `openPointInTime()` and its typed response again; the agreed PIT pagination is unchanged.
  The aligned CI run progressed to search requests (see below). Do not infer continued 8.8 compatibility from this
  change. No local Docker execution was attempted.
- Earlier user-supplied CI log (2026-10-30, aligned versions): **12 tests, 5 search errors** from
  explicitly serialized `search_after: []`. Fixed by setting it only when sort values exist; the
  first request now omits it and later requests retain full server sort values. JSON assertions
  cover both shapes. All 43 non-container tests pass; the subsequent CI run passed per the user.
- Integration preparation completed: `AbstractElasticTest.initAdaper()` replaces the example's
  connection with the container's sole endpoint. The import test uses a separate v2 allowlist,
  explicit destination and scoped read without mutating the shared adapter; refresh replaces sleep.
  Test bytecode was checked; the first CI run reached these tests but failed on PIT opening.
  Additional real-server coverage of new multi-index/alias/PIT/fallback cases remains pending.
- `git diff --check` passed at the implementation checkpoint; rerun after subsequent edits.

For subsequent runs, use the targeted command in the configuration guide; request approval before
network downloads or expensive/container suites. Do not assume ignored verification helpers still exist.

## CI: integration route

`.github/workflows/run-tests.yml` now defines independent checks on Ubuntu/JDK 21:

- Formatting (10-minute limit), non-container tests (10 minutes), and Elasticsearch integration
  (20 minutes). Formatting failure does not prevent tests; matrix fail-fast is disabled.
- Non-container selection: `!ElasticsearchAdapterTest,!TextFinderTest`. Integration selection:
  `ElasticsearchAdapterTest,TextFinderTest`; Testcontainers starts Elasticsearch 8.19.23 and helpers.
  No external Elasticsearch service is required. Keep these lists paired when adding container tests.
- Both suites upload `target/surefire-reports/` even after test failure, when available; artifacts
  `surefire-unit` / `surefire-integration` expire after 7 days.
- The existing `test` check remains an aggregate gate requiring formatting and both suites to pass.
- Main pushes/PRs and manual dispatch are supported. Manual dispatch becomes available once the
  workflow is on the default branch. Publishing/release workflows are unchanged.

YAML parsing, triggers, suite partition, and aggregate dependencies were checked locally using a cached
parser; this is not an Actions schema/actionlint validation. CI failures and the subsequent successful
run are recorded above from user reports; no run was triggered or fetched by the agent.
Package access uses existing CI secrets (values not inspected); fork PRs may lack them. Inspect all
checks and download reports on failure; workflow configuration alone is not test evidence.

## Keep the handoff current

- Read `git status` and relevant source before editing; do not overwrite user changes or assume commits.
- Update decision/review status here only when the user actually agrees or a new check is run.
- Update cross-project task status when that project's code changes; recheck its callers before claiming completion.
- Resolve/retire legacy IDs only after their removal gates are met; retain historical-data considerations.
- Keep detailed usage in the configuration guide, not duplicated throughout `docs/`.
