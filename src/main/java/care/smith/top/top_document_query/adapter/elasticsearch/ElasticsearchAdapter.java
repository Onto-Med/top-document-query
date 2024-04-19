package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.DocumentHit;
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
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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


import javax.print.Doc;

public class ElasticsearchAdapter extends TextAdapter {
  //ToDo: bug: es server returns by default (if no size is given) only the first ten hits.
  // And even if size is given 10.000 documents is max and not very efficient.
  // ES Client should have something like search_after which should do the trick -> adapt code accordingly
  //ToDo: fuzzy matching for terms
  private static final int DEFAULT_BATCH_SIZE = 20;
  private final Logger LOGGER = Logger.getLogger(ElasticsearchAdapter.class.getName());
  private ElasticsearchClient esClient;

  public ElasticsearchAdapter(TextAdapterConfig config) {
    super(config);
    initConnection();
  }

  public ElasticsearchAdapter(String configFile) {
    super(configFile);
    initConnection();
  }

  @Override
  public List<DocumentHit> execute(ConceptQuery query, Entities entities) {
    String queryString =
        Expressions.getStringValue(
            ElasticsearchSong.get()
                .concepts(entities)
                .lang(query.getLanguage())
                .generate(query.getEntityId()));
    return execute(queryString);
  }

  @Override
  public List<DocumentHit> execute(String queryString) {
    SearchResponse<DocumentEntity> searchResponse;
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
                                          .fields(Arrays.asList(config.getField())))),
              DocumentEntity.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return searchResponse.hits().hits().stream()
        .map(hit -> new DocumentHit(hit.id(), hit.source(), hit.score()))
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
  public Stream<List<Document>> getAllDocumentsBatched(Integer batchSize) {
    int bs = prepareBatchSize(batchSize);
    return Stream.generate(
            new Supplier<List<Document>>() {
              List<FieldValue> sortValues = List.of(FieldValue.FALSE);
              @Override
              public List<Document> get() {
                try {
                  FieldSort fs = new FieldSort.Builder().field("name").order(SortOrder.Asc).build();
                  SearchResponse<DocumentEntity> response =
                      esClient.search(
                          s ->
                              s.index(Arrays.asList(config.getIndex()))
                                  .query(matchAllQuery())
                                  .sort(sb -> sb.field(fs))
                                  .size(bs)
                                  .searchAfter(sortValues),
                          DocumentEntity.class);
                  List<Hit<DocumentEntity>> hits = response.hits().hits();
                  sortValues = getLastSortValues(hits);
                  return hits.stream()
                      .map(Hit::source)
                      .filter(Objects::nonNull)
                      .map(DocumentEntity::toApiModel)
                      .collect(Collectors.toList());
                } catch (IOException e) {
                  return List.of();
                }
              }
            })
        .takeWhile(list -> !list.isEmpty());
  }

  /**
   * Get all documents in a specific page.
   *
   * @param page The page number, if negative, method returns all entries.
   * @return List of Document entries of the page.
   */
  public Page<Document> getAllDocuments(Integer page) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchRequest.Builder sb = new SearchRequest.Builder()
        .index(Arrays.asList(config.getIndex()));

    SearchResponse<DocumentEntity> response;
    if (page != null && page >= 0) {
      response = esClient.search(s -> sb.from(page * batchSize).size(batchSize), DocumentEntity.class);
    } else {
      response = esClient.search(s -> sb, DocumentEntity.class);
    }
    return toPage(response, page, true);
  }

  /**
   * Return a single document by its ID.
   *
   * @param documentId The ID to search for.
   * @return {@link java.util.Optional<DocumentEntity>}.
   * @throws IOException If request to ES failed.
   */
  public Optional<Document> getDocumentById(@NonNull String documentId) throws IOException {
    //ToDo: right now the adapter config allows for multiple index values (as an array),
    // but only the first index value will be used here (e.g. GetResponse needs an index name as parameter)
    // the .search method allows for List of indices however
    GetResponse<DocumentEntity> response =
        esClient.get(g -> g.id(documentId).index(config.getIndex()[0]), DocumentEntity.class);
    if (response.found() && response.source() != null) {
      return Optional.of(response.source().getId() == null ? response.source().toApiModel(response.id()) : response.source().toApiModel());
    } else {
      return Optional.empty();
    }
  }

  public Page<Document> getDocumentsByName(@NonNull String documentName, Integer page)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    final String finalDocumentName = documentName.trim();
    if (finalDocumentName.isEmpty()) {
      return getAllDocuments(page);
    }
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s
                  .index(Arrays.asList(config.getIndex()))
                  .query(q ->
                      q.wildcard(
                          new WildcardQuery.Builder()
                              .field(DocumentFields.TITLE.getValue())
                              .wildcard(finalDocumentName + "*")
                              .caseInsensitive(true)
                              .build()));
            },
            DocumentEntity.class);
    return toPage(response, page, true);
  }

  public Page<Document> getDocumentsByIds(@NonNull Collection<String> ids, Integer page)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(config.getIndex())).query(queryForIds(ids));
            },
            DocumentEntity.class);
    return toPage(response, page, true);
  }

  public Stream<Document> getDocumentsByIds(@NonNull Collection<String> ids)
      throws IOException {
    return null;
  }

  @Override
  public Page<Document> getDocumentsByTerms(@NonNull Collection<String> terms, Integer page)
      throws IOException {
    return getDocumentsByTerms(terms, TermConcatenationTypes.AND, page);
  }

  public Page<Document> getDocumentsByTerms(@NonNull Collection<String> terms, TermConcatenationTypes concatenationTypes, Integer page)
      throws IOException {
    String queryString = queryStringByConcatenationType(terms, concatenationTypes);
    int batchSize = prepareBatchSize(config.getBatchSize());

    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(config.getIndex())).query(queryForQueryString(queryString));
            },
            DocumentEntity.class);
    return toPage(response, page, true);
  }

  public Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids, @NonNull Collection<String> terms, Integer page) throws IOException {
    return getDocumentsByIdsAndTerms(ids, terms, TermConcatenationTypes.AND, page);
  }

  @Override
  public Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids, @NonNull Collection<String> terms, TermConcatenationTypes concatenationTypes, Integer page) throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    String queryString = queryStringByConcatenationType(terms, concatenationTypes);

    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page >= 0) s.from(page * batchSize).size(batchSize);
              return s.index(Arrays.asList(config.getIndex())).query(q -> q.bool(BoolQuery.of(
                  qb -> qb.filter(queryForQueryString(queryString), queryForIds(ids))
              )));
            },
            DocumentEntity.class);
    return toPage(response, page, true);
  }

  private String queryStringByConcatenationType(
      @NonNull Collection<String> terms, TermConcatenationTypes concatenationTypes) {
    if (concatenationTypes == null) concatenationTypes = TermConcatenationTypes.AND;
    String queryString;
    if (concatenationTypes.equals(TermConcatenationTypes.AND)) {
      queryString = terms.stream()
          .map(s -> String.format("+%s", s))
          .collect(Collectors.joining(" "));
    } else {
      queryString = String.join(" | ", terms);
    }
    return queryString;
  }

  private List<FieldValue> getLastSortValues(List<Hit<DocumentEntity>> hits) {
    if (hits.isEmpty()) return List.of(FieldValue.FALSE);
    Hit<DocumentEntity> lastHit = hits.get(hits.size() - 1);
//    FieldValue documentId = (lastHit.source() != null) ? FieldValue.of(lastHit.source().getId()) : FieldValue.NULL;
    FieldValue documentName = (lastHit.source() != null) ? FieldValue.of(lastHit.source().getName()) : FieldValue.NULL;
//    return List.of(documentId, documentName);
    return List.of(documentName);
  }

  private SortOptions defaultSort() {
    return SortOptions.of(sob -> sob.field(fb ->
        fb.field("id").order(SortOrder.Asc).field("name").order(SortOrder.Asc))
    );
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

  private Consumer<Hit<DocumentEntity>> documentCollector(Collection<Document> content, Boolean simplified) {
    return r -> {
      if (r.source() == null) return;
      Document document =
          r.source().getId() == null
              ? (simplified ? r.source().toSimplifiedApiModel(r.id()) : r.source().toApiModel(r.id()))
              : (simplified ? r.source().toSimplifiedApiModel() : r.source().toApiModel());
      content.add(document);
    };
  }

  private Page<Document> toPage(SearchResponse<DocumentEntity> response, Integer page, Boolean simplified) {
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
