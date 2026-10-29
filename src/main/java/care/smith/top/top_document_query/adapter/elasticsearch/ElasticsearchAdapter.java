package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.*;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.DocumentReference;
import care.smith.top.top_document_query.adapter.IndexSelection;
import care.smith.top.top_document_query.adapter.IndexedDocument;
import care.smith.top.top_document_query.adapter.config.Connection;
import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import care.smith.top.top_document_query.elasticsearch.DocumentFields;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.URI;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class ElasticsearchAdapter extends TextAdapter implements AutoCloseable {
  // ToDo: fuzzy matching for terms
  private final int DEFAULT_BATCH_SIZE;
  private final Logger LOGGER = Logger.getLogger(ElasticsearchAdapter.class.getName());
  private ElasticsearchClient esClient;
  private final IndexSelection selection;
  private final boolean ownsClient;

  public ElasticsearchAdapter(TextAdapterConfig config) {
    super(config);
    config.validate();
    this.DEFAULT_BATCH_SIZE = config.getBatchSize();
    this.selection = null;
    this.ownsClient = true;
    initConnection();
  }

  public ElasticsearchAdapter(String configFile) {
    this(TextAdapterConfig.getInstance(configFile));
  }

  /** Use an existing client without taking ownership (also useful for offline transport tests). */
  public ElasticsearchAdapter(TextAdapterConfig config, ElasticsearchClient client) {
    this(config, client, null);
  }

  private ElasticsearchAdapter(
      TextAdapterConfig config, ElasticsearchClient client, IndexSelection selection) {
    super(config);
    config.validate();
    this.DEFAULT_BATCH_SIZE = config.getBatchSize();
    this.esClient = Objects.requireNonNull(client);
    this.selection = selection;
    this.ownsClient = false;
  }

  /** Scoped views share the client, not mutable selection state. The owner must outlive its views. */
  @Override
  public ElasticsearchAdapter forIndices(Collection<String> indices) {
    IndexSelection selected = IndexSelection.select(config, indices);
    if (selection != null && !selection.indices().containsAll(selected.indices())) {
      throw new IllegalArgumentException("Cannot expand an existing index scope");
    }
    return new ElasticsearchAdapter(config, esClient, selected);
  }

  @Override
  public ElasticsearchAdapter forIndex(String index) {
    return forIndices(List.of(index));
  }

  public List<String> getSelectedIndices() {
    return readSelection().indices();
  }

  private IndexSelection readSelection() {
    return selection == null ? IndexSelection.defaultRead(config) : selection;
  }

  private String writeIndex() {
    return (selection == null ? IndexSelection.configured(config) : selection).singleIndex();
  }

  @Override
  public void close() throws IOException {
    if (ownsClient) esClient._transport().close();
  }

  @Override
  public Map<String, Integer> getSubconceptDepths(ConceptQuery query, Entities concepts) {
    return ElasticsearchSong.get()
        .concepts(concepts)
        .checkForSubconceptResolution(query.getEntityId());
  }

  @Override
  public Stream<List<DocumentHit>> execute(
      ConceptQuery query, Map<String, Entity> entities, Map<String, Set<String>> dependencies) {
    // ToDo: somehow need to check how large a query becomes and split it up (or reject it)
    Expression esExp =
        ElasticsearchSong.get()
            .concepts(buildConceptHierarchy(entities, dependencies))
            .lang(query.getLanguage())
            .generate(query.getEntityId());

    if (esExp.getValues().size() > 1) {
      return execute(
          Expressions.getStringValues(esExp).stream()
              //              .map(s -> StringUtils.containsWhitespace(s)? String.format("\"%s\"",
              // s))
              .collect(Collectors.joining(" OR ")),
          false);
    } else {
      boolean exactMatch = entities.size() == 1;
      return execute(
          String.format(exactMatch ? "\"%s\"" : "%s", Expressions.getStringValue(esExp)),
          exactMatch);
    }
  }

  @Override
  public Stream<List<DocumentHit>> execute(String queryString) {
    return execute(queryString, true);
  }

  public Stream<List<DocumentHit>> execute(String queryString, boolean exactHighlight) {
    Query query =
        QueryStringQuery.of(q -> q.query(queryString).fields(Arrays.asList(config.getField())))
            ._toQuery();
    Highlight highlight =
        Highlight.of(
            h ->
                h.type(HighlighterType.Unified)
                    .fields(
                        Arrays.stream(config.getField())
                            .map(
                                f ->
                                    new HashMap<String, HighlightField>() {
                                      {
                                        put(
                                            f,
                                            HighlightField.of(
                                                hf -> hf.numberOfFragments(0).fragmentSize(30)));
                                      }
                                    })
                            .reduce(
                                (firstMap, secondMap) -> {
                                  firstMap.putAll(secondMap);
                                  return firstMap;
                                })
                            .orElseThrow()));
    SearchCursor cursor = new SearchCursor();
    return Stream.generate(
            new Supplier<List<DocumentHit>>() {
              final int bs = prepareBatchSize(config.getBatchSize());

              @Override
              public List<DocumentHit> get() {
                try {
                  List<Hit<DocumentEntity>> hits =
                      cursor.next(query, highlight, bs);
                  return hits.stream()
                      .map(
                          hit ->
                              new DocumentHit(
                                  hit.id(),
                                  hit.source(),
                                  exactHighlight
                                      ? mergeHighlights(hit.highlight(), queryString)
                                      : hit.highlight(),
                                  hit.score()).setIndex(hit.index()).setDataSourceId(config.getId()))
                      .toList();
                } catch (IOException e) {
                  throw new UncheckedIOException("Could not retrieve query results", e);
                }
              }
            })
        .takeWhile(list -> !list.isEmpty())
        .onClose(cursor::close);
  }

  private Map<String, List<String>> mergeHighlights(
      Map<String, List<String>> highlights, String queryString) {
    String[] queryComponents =
        queryString
            .substring(
                queryString.charAt(0) == '"' ? 1 : 0,
                queryString.charAt(queryString.length() - 1) == '"'
                    ? queryString.length() - 1
                    : queryString.length())
            .split("\s+");
    HashMap<String, List<String>> mergedHighlights = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
      ArrayList<String> newHighlights = new ArrayList<>();
      for (String highlight : entry.getValue()) {
        StringBuilder highlightBuilder = new StringBuilder(highlight);
        Pattern pattern =
            Pattern.compile(
                Arrays.stream(queryComponents)
                    .map(s -> String.format("<em>%s</em>", s))
                    .collect(Collectors.joining("(\s+)")));
        Matcher matcher = pattern.matcher(highlight);
        while (matcher.find()) {
          StringBuilder replBuilder = new StringBuilder();
          replBuilder.append(queryComponents[0]);
          for (int i = 1; i <= matcher.groupCount(); i++) {
            replBuilder.append(matcher.group(i)).append(queryComponents[i]);
          }
          highlightBuilder.replace(
              matcher.start(), matcher.end(), String.format("<em>%s</em>", replBuilder));
          newHighlights.add(highlightBuilder.toString());
        }
      }
      mergedHighlights.put(entry.getKey(), newHighlights);
    }
    return mergedHighlights;
  }

  @Override
  public Page<IndexedDocument> getAllIndexedDocumentsPaged(Integer page, Boolean simplified)
      throws IOException {
    return searchIndexedDocuments(matchAllQuery(), page, simplified);
  }

  @Override
  public Page<IndexedDocument> getIndexedDocumentsByIdsPaged(
      Collection<String> ids, Integer page, Boolean simplified) throws IOException {
    return searchIndexedDocuments(queryForIds(ids), page, simplified);
  }

  @Override
  public Page<IndexedDocument> getIndexedDocumentsByNamePaged(
      String name, Integer page, Boolean simplified) throws IOException {
    Query query = name.trim().isEmpty() ? matchAllQuery() : WildcardQuery.of(
        q -> q.field(DocumentFields.TITLE.getValue()).wildcard(name.trim() + "*")
            .caseInsensitive(true))._toQuery();
    return searchIndexedDocuments(query, page, simplified);
  }

  @Override
  public Stream<List<IndexedDocument>> getAllIndexedDocumentsBatched(
      Integer batchSize, Boolean simplified) {
    return streamIndexedDocuments(matchAllQuery(), batchSize, simplified);
  }

  @Override
  public Stream<List<IndexedDocument>> getIndexedDocumentsByIdsBatched(
      Collection<String> ids, Integer batchSize, Boolean simplified) {
    return streamIndexedDocuments(queryForIds(ids), batchSize, simplified);
  }

  /** Advanced read API for term/combined filters without discarding each hit's identity. */
  public Page<IndexedDocument> searchIndexedDocuments(
      Query query, Integer page, Boolean simplified) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response = esClient.search(s -> {
      s.index(Arrays.asList(indexArrayStringConformity())).query(query).size(batchSize)
          .trackTotalHits(t -> t.enabled(true));
      if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
      return s;
    }, DocumentEntity.class);
    return toIndexedPage(response, page, simplified);
  }

  @Override
  public long count() {
    try {
      return esClient.count(s -> s.index(Arrays.asList(indexArrayStringConformity()))).count();
    } catch (IOException e) {
      LOGGER.warning(e.getMessage());
      return 0;
    }
  }

  /**
   * Returns all documents as batches of the specified size.
   *
   * @param batchSize Size of each list element returned from the stream.
   * @return A stream consisting of lists with size 'batchSize'.
   */
  @Override
  public Stream<List<Document>> getAllDocumentsBatched(Integer batchSize, Boolean simplified) {
    return documentStream(matchAllQuery(), batchSize, simplified);
  }

  /**
   * Get all documents in a specific page.
   *
   * @param page The page number, if negative, method returns all entries.
   * @return List of Document entries of the page.
   */
  @Override
  public Page<Document> getAllDocumentsPaged(Integer page, Boolean simplified) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchRequest.Builder sb =
        new SearchRequest.Builder().index(Arrays.asList(indexArrayStringConformity()));

    SearchResponse<DocumentEntity> response;
    if (page != null && page >= 0) {
      response =
          esClient.search(s -> sb.from(page * batchSize).size(batchSize), DocumentEntity.class);
    } else {
      response = esClient.search(s -> sb, DocumentEntity.class);
    }
    return toPage(response, page, simplified);
  }

  @Override
  public Page<Document> getDocumentsByNamePaged(
      @NonNull String documentName, Integer page, Boolean simplified) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    final String finalDocumentName = documentName.trim();
    if (finalDocumentName.isEmpty()) {
      return getAllDocumentsPaged(page, simplified);
    }
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(indexArrayStringConformity()))
                  .query(
                      q ->
                          q.wildcard(
                              new WildcardQuery.Builder()
                                  .field(DocumentFields.TITLE.getValue())
                                  .wildcard(finalDocumentName + "*")
                                  .caseInsensitive(true)
                                  .build()));
            },
            DocumentEntity.class);
    return toPage(response, page, simplified);
  }

  @Override
  public Stream<List<Document>> getDocumentsByNameBatched(
      @NonNull String documentName, Integer batchSize, Boolean simplified) {
    final String finalDocumentName = documentName.trim();
    if (finalDocumentName.isEmpty()) {
      return getAllDocumentsBatched(batchSize, simplified);
    }
    Query wcq =
        WildcardQuery.of(
                q ->
                    q.field(DocumentFields.TITLE.getValue())
                        .wildcard(finalDocumentName + "*")
                        .caseInsensitive(true))
            ._toQuery();
    return documentStream(wcq, batchSize, simplified);
  }

  /**
   * Return a single document by its ID.
   *
   * @param documentId The ID to search for.
   * @return {@link java.util.Optional<DocumentEntity>}.
   * @throws IOException If request to ES failed.
   */
  @Override
  public Optional<Document> getDocumentById(@NonNull String documentId, Boolean simplified)
      throws IOException {
    String index = readSelection().singleIndex();
    SearchResponse<DocumentEntity> response = esClient.search(
        s -> s.index(index).query(queryForIds(List.of(documentId))).size(2), DocumentEntity.class);
    if (response.hits().hits().size() > 1) {
      throw new IllegalStateException("Document ID is ambiguous; select a concrete index rather than a multi-index alias");
    }
    List<Document> documents = new ArrayList<>();
    response.hits().hits().forEach(documentCollector(documents, simplified));
    return documents.stream().findFirst();
  }

  /** Resolve concrete hit provenance through the allowed scope, including configured aliases. */
  @Override
  public Optional<Document> getDocument(DocumentReference reference, Boolean simplified)
      throws IOException {
    if (!Objects.equals(config.getId(), reference.dataSourceId())) {
      throw new IllegalArgumentException("Document reference belongs to a different data source");
    }
    List<String> allowed = (selection == null ? IndexSelection.configured(config) : selection).indices();
    SearchResponse<DocumentEntity> response = esClient.search(
        s -> s.index(allowed).size(2).query(q -> q.bool(b -> b.filter(
            queryForIds(List.of(reference.documentId())),
            TermQuery.of(t -> t.field("_index").value(reference.index()))._toQuery()))),
        DocumentEntity.class);
    if (response.hits().hits().size() > 1) {
      throw new IllegalStateException("Document reference did not resolve uniquely");
    }
    List<Document> documents = new ArrayList<>();
    response.hits().hits().forEach(documentCollector(documents, simplified));
    return documents.stream().findFirst();
  }

  @Override
  public Page<Document> getDocumentsByIdsPaged(
      @NonNull Collection<String> ids, Integer page, Boolean simplified) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(indexArrayStringConformity())).query(queryForIds(ids));
            },
            DocumentEntity.class);
    return toPage(response, page, simplified);
  }

  @Override
  public Stream<List<Document>> getDocumentsByIdsBatched(
      @NonNull Collection<String> ids, Integer batchSize, Boolean simplified) {
    return documentStream(queryForIds(ids), batchSize, simplified);
  }

  @Override
  public Page<Document> getDocumentsByTerms(
      @NonNull Collection<String> terms, Integer page, Boolean simplified) throws IOException {
    return getDocumentsByTerms(terms, TermConcatenationTypes.AND, page, simplified);
  }

  @Override
  public Page<Document> getDocumentsByTerms(
      @NonNull Collection<String> terms,
      TermConcatenationTypes concatenationTypes,
      Integer page,
      Boolean simplified)
      throws IOException {
    String queryString = queryStringByConcatenationType(terms, concatenationTypes);
    int batchSize = prepareBatchSize(config.getBatchSize());

    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(indexArrayStringConformity()))
                  .query(queryForQueryString(queryString));
            },
            DocumentEntity.class);
    return toPage(response, page, simplified);
  }

  @Override
  public Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids,
      @NonNull Collection<String> terms,
      Integer page,
      Boolean simplified)
      throws IOException {
    return getDocumentsByIdsAndTerms(ids, terms, TermConcatenationTypes.AND, page, simplified);
  }

  @Override
  public Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids,
      @NonNull Collection<String> terms,
      TermConcatenationTypes concatenationTypes,
      Integer page,
      Boolean simplified)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    String queryString = queryStringByConcatenationType(terms, concatenationTypes);

    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(indexArrayStringConformity()))
                  .query(
                      q ->
                          q.bool(
                              BoolQuery.of(
                                  qb ->
                                      qb.filter(
                                          queryForQueryString(queryString), queryForIds(ids)))));
            },
            DocumentEntity.class);
    return toPage(response, page, simplified);
  }

  @Override
  public DocumentImport importDocuments(@NonNull Document[] documents, String language) {
    String index = writeIndex();
    DocumentImport documentImport = new DocumentImport();
    if (initDocumentIndex(index, language)) {
      BulkRequest.Builder br = new BulkRequest.Builder();
      for (Document document : documents) {
        br.operations(
            op ->
                op.index(
                    idx ->
                        idx.index(index)
                            .id(document.getId())
                            .document(new DocumentEntity().fromDocumentModel(document))));
      }
      BulkResponse result;
      try {
        result = esClient.bulk(br.build());
      } catch (IOException e) {
        LOGGER.severe(
            String.format(
                "Server Instance could not be reached/queried. Please check the Elasticsearch logs and/or the adapter configuration: %s",
                config));
        return documentImport
            .status(DocumentImportStatus.FAILED)
            .count(0)
            .documents(new ArrayList<>());
      }
      int successCount = 0;
      if (result.errors()) {
        LOGGER.warning("Document upload had errors:");
      }
      for (BulkResponseItem item : result.items()) {
        if (item.error() != null) {
          LOGGER.severe(item.error().reason());
        } else {
          documentImport.addDocumentsItem(item.id());
          successCount++;
        }
      }
      if (successCount >= result.items().size()) {
        documentImport.setStatus(DocumentImportStatus.SUCCESSFUL);
      } else if (successCount == 0) {
        documentImport.setStatus(DocumentImportStatus.FAILED);
        LOGGER.severe("All documents failed to upload.");
      } else {
        documentImport.setStatus(DocumentImportStatus.PARTIALLY);
        LOGGER.warning(
            String.format(
                "Not all documents were successfully uploaded. %s from %s failed.",
                result.items().size() - successCount, result.items().size()));
      }
    }
    return documentImport.count(
        documentImport.getDocuments() != null ? documentImport.getDocuments().size() : 0);
  }

  private boolean initDocumentIndex(String index, String language) {
    if (hasIndex(index)) {
      LOGGER.warning("Index already exists: " + index);
      return true;
    }
    try {
      esClient
          .indices()
          .create(
              c ->
                  c.index(index)
                      .mappings(
                          m ->
                              m.properties(
                                  Objects.requireNonNull(
                                          ElasticsearchIndexSettings.getMappings(language))
                                      .get("properties")))
                      .settings(
                          s ->
                              s.analysis(
                                  Objects.requireNonNull(
                                          ElasticsearchIndexSettings.getSettings(language))
                                      .get("analysis"))));
    } catch (IOException e) {
      return false;
    } catch (ElasticsearchException e) {
      return false;
    }
    return true;
  }

  private boolean hasIndex(String indexName) {
    try {
      return esClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
    } catch (IOException e) {
      LOGGER.severe("Could not connect to elasticsearch: " + config.getConnection().toString());
      return false;
    } catch (ElasticsearchException e) {
      LOGGER.severe("Some Elasticsearch Error: " + e.getMessage());
      return false;
    }
  }

  /** PIT plus _shard_doc makes ties safe across shards/indices; use try-with-resources on streams. */
  private final class SearchCursor implements AutoCloseable {
    private final List<String> indices = readSelection().indices();
    private String pitId;
    private List<FieldValue> sortValues = List.of();
    private boolean finished;

    List<Hit<DocumentEntity>> next(Query query, @Nullable Highlight highlight, int batchSize)
        throws IOException {
      if (finished) return List.of();
      try {
        if (pitId == null) {
          pitId = esClient.openPointInTime(
              p -> p.index(indices).keepAlive(t -> t.time("1m"))).id();
        }
        SearchResponse<DocumentEntity> response = esClient.search(
            s -> s.pit(p -> p.id(pitId).keepAlive(t -> t.time("1m")))
                .query(query).highlight(highlight)
                .sort(sort -> sort.field(f -> f.field("name.keyword").order(SortOrder.Asc)))
                .sort(sort -> sort.field(f -> f.field("_shard_doc").order(SortOrder.Asc)))
                .size(batchSize).searchAfter(sortValues),
            DocumentEntity.class);
        if (response.pitId() != null) pitId = response.pitId();
        List<Hit<DocumentEntity>> hits = response.hits().hits();
        if (!hits.isEmpty()) sortValues = hits.getLast().sort();
        if (hits.size() < batchSize) close();
        return hits;
      } catch (IOException | RuntimeException e) {
        close();
        throw e;
      }
    }

    @Override
    public void close() {
      if (finished) return;
      finished = true;
      if (pitId != null) {
        try {
          esClient.closePointInTime(p -> p.id(pitId));
        } catch (IOException | RuntimeException e) {
          LOGGER.fine("Could not close search PIT; it will expire after its keep-alive");
        }
      }
    }
  }

  private Stream<List<Document>> documentStream(
      Query query, Integer batchSize, Boolean simplified) {
    return streamIndexedDocuments(query, batchSize, simplified)
        .map(batch -> batch.stream().map(IndexedDocument::document).toList());
  }

  public Stream<List<IndexedDocument>> streamIndexedDocuments(
      Query query, Integer batchSize, Boolean simplified) {
    SearchCursor cursor = new SearchCursor();
    return Stream.generate(() -> {
          try {
            return cursor.next(query, null, prepareBatchSize(batchSize));
          } catch (IOException e) {
            throw new UncheckedIOException("Could not retrieve documents", e);
          }
        })
        .takeWhile(hits -> !hits.isEmpty())
        .map(hits -> hits.stream().filter(hit -> hit.source() != null)
            .map(hit -> indexedDocument(hit, simplified)).toList())
        .onClose(cursor::close);
  }

  private String queryStringByConcatenationType(
      @NonNull Collection<String> terms, TermConcatenationTypes concatenationTypes) {
    if (concatenationTypes == null) concatenationTypes = TermConcatenationTypes.AND;
    String queryString;
    if (concatenationTypes.equals(TermConcatenationTypes.AND)) {
      queryString =
          terms.stream().map(s -> String.format("+%s", s)).collect(Collectors.joining(" "));
    } else {
      queryString = String.join(" | ", terms);
    }
    return queryString;
  }

  @SuppressWarnings("unused")
  private SortOptions defaultSort() {
    return SortOptions.of(
        sob ->
            sob.field(
                fb -> fb.field("id").order(SortOrder.Asc).field("name").order(SortOrder.Asc)));
  }

  private Query matchAllQuery() {
    return MatchAllQuery.of(mq -> mq)._toQuery();
  }

  private Query queryForIds(Collection<String> ids) {
    return IdsQuery.of(iq -> iq.values(new ArrayList<>(ids)))._toQuery();
  }

  private Query queryForQueryString(String queryString) {
    return SimpleQueryStringQuery.of(sq -> sq.query(queryString))._toQuery();
  }

  private int prepareBatchSize(Integer batchSize) {
    return batchSize == null || batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
  }

  private IndexedDocument indexedDocument(Hit<DocumentEntity> hit, Boolean simplified) {
    DocumentEntity source = hit.source();
    Document document = source.getId() == null
        ? (Boolean.TRUE.equals(simplified) ? source.toSimplifiedApiModel(hit.id()) : source.toApiModel(hit.id()))
        : (Boolean.TRUE.equals(simplified) ? source.toSimplifiedApiModel() : source.toApiModel());
    return new IndexedDocument(new DocumentReference(config.getId(), hit.index(), hit.id()), document);
  }

  private Consumer<Hit<DocumentEntity>> documentCollector(
      Collection<Document> content, Boolean simplified) {
    return hit -> {
      if (hit.source() != null) content.add(indexedDocument(hit, simplified).document());
    };
  }

  private Page<Document> toPage(
      SearchResponse<DocumentEntity> response, Integer page, Boolean simplified) {
    return toIndexedPage(response, page, simplified).map(IndexedDocument::document);
  }

  private Page<IndexedDocument> toIndexedPage(
      SearchResponse<DocumentEntity> response, Integer page, Boolean simplified) {
    List<IndexedDocument> content = response.hits().hits().stream()
        .filter(hit -> hit.source() != null).map(hit -> indexedDocument(hit, simplified)).toList();
    PageRequest pageRequest =
        page == null || page < 1
            ? PageRequest.ofSize(prepareBatchSize(config.getBatchSize()))
            : PageRequest.of(page, prepareBatchSize(config.getBatchSize()));
    long total = response.hits().total() != null ? response.hits().total().value() : 0;
    return new PageImpl<>(content, pageRequest, total);
  }

  private void initConnection() {
    for (String endpoint : config.getConnection().getEndpoints()) {
      URI uri = Connection.validateEndpoint(endpoint);
      RestClient restClient = RestClient.builder(
          new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())).build();
      ElasticsearchTransport transport =
          new RestClientTransport(restClient, new JacksonJsonpMapper());
      ElasticsearchClient candidate = new ElasticsearchClient(transport);
      try {
        candidate.info();
        this.esClient = candidate;
        return;
      } catch (IOException | RuntimeException e) {
        try {
          transport.close();
        } catch (IOException closeError) {
          LOGGER.fine("Could not close failed Elasticsearch transport");
        }
        LOGGER.warning("Elasticsearch endpoint unavailable; trying the next configured endpoint");
      }
    }
    throw new IllegalStateException("Could not connect to any configured Elasticsearch endpoint");
  }

  private String[] indexArrayStringConformity() {
    return readSelection().indices().toArray(String[]::new);
  }
}
