package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Document;
import care.smith.top.model.Expression;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import care.smith.top.top_document_query.elasticsearch.DocumentFields;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
import care.smith.top.top_document_query.util.builder.Exp;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;

public class ElasticsearchAdapter extends TextAdapter {
  // ToDo: fuzzy matching for terms
  private final int DEFAULT_BATCH_SIZE;
  private final Logger LOGGER = Logger.getLogger(ElasticsearchAdapter.class.getName());
  private ElasticsearchClient esClient;

  public ElasticsearchAdapter(TextAdapterConfig config) {
    super(config);
    this.DEFAULT_BATCH_SIZE = config.getBatchSize();
    initConnection();
  }

  public ElasticsearchAdapter(String configFile) {
    super(configFile);
    this.DEFAULT_BATCH_SIZE = config.getBatchSize();
    initConnection();
  }

  @Override
  public List<DocumentHit> execute(ConceptQuery query, Entities entities) {
    Expression esExp = ElasticsearchSong.get()
        .concepts(entities)
        .lang(query.getLanguage())
        .generate(query.getEntityId());

    String queryString;
    if (esExp.getValues().size() > 1) {
      queryString = Expressions.getStringValues(esExp)
              .stream()
              .map(s -> String.format("\"%s\"", s))
              .collect(Collectors.joining(" OR "));
    } else {
      queryString = Expressions.getStringValue(esExp);
    }
    return execute(queryString);
  }

  // ToDo: should not return List but rather batched/paged like the other methods
  @Override
  public List<DocumentHit> execute(String queryString) {
    SearchResponse<DocumentEntity> searchResponse;
    // ToDo: shall the highlighting be hard-coded? Or in adapter config?
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
                                                hf -> hf.numberOfFragments(100).fragmentSize(30)));
                                      }
                                    })
                            .reduce(
                                (firstMap, secondMap) -> {
                                  firstMap.putAll(secondMap);
                                  return firstMap;
                                })
                            .orElseThrow()));
    try {
      searchResponse =
          esClient.search(
              s ->
                  s.index(Arrays.asList(config.getIndex()))
                      .query(
                          q ->
                              q.queryString(
                                  qs ->
                                      qs.query(queryString)
                                          .fields(Arrays.asList(config.getField()))))
                      .highlight(highlight),
              DocumentEntity.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return searchResponse.hits().hits().stream()
        .map(hit -> new DocumentHit(hit.id(), hit.source(), hit.highlight(), hit.score()))
        .collect(Collectors.toList());
  }

  @Override
  public long count() {
    try {
      return esClient.count(s -> s.index(Arrays.asList(config.getIndex()))).count();
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
    return Stream.generate(documentSupplier(matchAllQuery(), batchSize, simplified))
        .takeWhile(list -> !list.isEmpty());
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
    SearchRequest.Builder sb = new SearchRequest.Builder().index(Arrays.asList(config.getIndex()));

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
              return s.index(Arrays.asList(config.getIndex()))
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
    return Stream.generate(documentSupplier(wcq, batchSize, simplified))
        .takeWhile(list -> !list.isEmpty());
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
    // ToDo: right now the adapter config allows for multiple index values (as an array),
    // but only the first index value will be used here (e.g. GetResponse needs an index name as
    // parameter)
    // the .search method allows for List of indices however
    GetResponse<DocumentEntity> response =
        esClient.get(g -> g.id(documentId).index(config.getIndex()[0]), DocumentEntity.class);
    if (response.found() && response.source() != null) {
      if (!simplified)
        return Optional.of(
            response.source().getId() == null
                ? response.source().toApiModel(response.id())
                : response.source().toApiModel());
      return Optional.of(
          response.source().getId() == null
              ? response.source().toSimplifiedApiModel(response.id())
              : response.source().toSimplifiedApiModel());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Page<Document> getDocumentsByIdsPaged(
      @NonNull Collection<String> ids, Integer page, Boolean simplified) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(config.getIndex())).query(queryForIds(ids));
            },
            DocumentEntity.class);
    return toPage(response, page, simplified);
  }

  @Override
  public Stream<List<Document>> getDocumentsByIdsBatched(
      @NonNull Collection<String> ids, Integer batchSize, Boolean simplified) {
    return Stream.generate(documentSupplier(queryForIds(ids), batchSize, simplified))
        .takeWhile(list -> !list.isEmpty());
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
              return s.index(Arrays.asList(config.getIndex()))
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
              return s.index(Arrays.asList(config.getIndex()))
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

  private Supplier<List<Document>> documentSupplier(
      Query query, Integer batchSize, Boolean simplified) {
    return new Supplier<>() {
      List<FieldValue> sortValues = List.of(FieldValue.of(""));
      final FieldSort fs =
          new FieldSort.Builder().field("name.keyword").order(SortOrder.Asc).build();
      final int bs = prepareBatchSize(batchSize);

      @Override
      public List<Document> get() {
        try {
          SearchResponse<DocumentEntity> response =
              esClient.search(
                  s ->
                      s.index(Arrays.asList(config.getIndex()))
                          .query(query)
                          .sort(sb -> sb.field(fs))
                          .size(bs)
                          .searchAfter(sortValues),
                  DocumentEntity.class);
          List<Hit<DocumentEntity>> hits = response.hits().hits();
          sortValues = getLastSortValues(hits);
          List<Document> documents = new ArrayList<>();
          hits.forEach(
              documentEntityHit -> {
                DocumentEntity de = documentEntityHit.source();
                if (de == null) return;
                Document finalDocument;
                if (de.getId() != null || documentEntityHit.id() == null) {
                  finalDocument = (simplified) ? de.toSimplifiedApiModel() : de.toApiModel();
                } else {
                  finalDocument =
                      (simplified)
                          ? de.toSimplifiedApiModel(documentEntityHit.id())
                          : de.toApiModel(documentEntityHit.id());
                }
                documents.add(finalDocument);
              });
          return documents;
        } catch (IOException e) {
          LOGGER.fine(String.format("Could not retrieve documents for query:\n'%s'", query));
          return List.of();
        }
      }
    };
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

  private List<FieldValue> getLastSortValues(List<Hit<DocumentEntity>> hits) {
    // ToDo: I honestly have no idea for what FieldValue NULL, FALSE or TRUE are
    if (hits.isEmpty()) return List.of(FieldValue.FALSE);
    Hit<DocumentEntity> lastHit = hits.get(hits.size() - 1);
    //    FieldValue documentId = (lastHit.source() != null) ?
    // FieldValue.of(lastHit.source().getId()) : FieldValue.NULL;
    FieldValue documentName =
        (lastHit.source() != null) ? FieldValue.of(lastHit.source().getName()) : FieldValue.NULL;
    //    return List.of(documentId, documentName);
    return List.of(documentName);
  }

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

  private Consumer<Hit<DocumentEntity>> documentCollector(
      Collection<Document> content, Boolean simplified) {
    return r -> {
      if (r.source() == null) return;
      Document document =
          r.source().getId() == null
              ? (simplified
                  ? r.source().toSimplifiedApiModel(r.id())
                  : r.source().toApiModel(r.id()))
              : (simplified ? r.source().toSimplifiedApiModel() : r.source().toApiModel());
      content.add(document);
    };
  }

  private Page<Document> toPage(
      SearchResponse<DocumentEntity> response, Integer page, Boolean simplified) {
    List<Document> content = new ArrayList<>();
    response.hits().hits().forEach(documentCollector(content, simplified));
    PageRequest pageRequest =
        page == null || page < 1
            ? PageRequest.ofSize(prepareBatchSize(config.getBatchSize()))
            : PageRequest.of(page, prepareBatchSize(config.getBatchSize()));
    long total = response.hits().total() != null ? response.hits().total().value() : 0;
    return new PageImpl<>(content, pageRequest, total);
  }

  private void initConnection() {
    String host;

    try {
      URL url = new URL(config.getConnection().getUrl());
      host = url.getHost();
    } catch (MalformedURLException e) {
      host = config.getConnection().getUrl();
    }

    RestClient restClient =
        RestClient.builder(new HttpHost(host, Integer.parseInt(config.getConnection().getPort())))
            .build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    this.esClient = new ElasticsearchClient(transport);
  }
}
