package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.ElasticDocument;
import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import care.smith.top.top_document_query.elasticsearch.DocumentFields;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;

public class ElasticsearchAdapter extends TextAdapter {
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
    SearchResponse<ElasticDocument> searchResponse;
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
              ElasticDocument.class);
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
      return esClient.count().count();
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
    return Stream.generate(
            new Supplier<List<Document>>() {
              int page = 0;

              @Override
              public List<Document> get() {
                try {
                  int bs = prepareBatchSize(batchSize);
                  return esClient
                      .search(s -> s.from(page++ * bs).size(bs), DocumentEntity.class)
                      .hits()
                      .hits()
                      .stream()
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
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> (page == null || page < 0) ? s : s.from(page * batchSize).size(batchSize),
            DocumentEntity.class);
    return toPage(response, page);
  }

  /**
   * Return a single document by its ID.
   *
   * @param documentId The ID to search for.
   * @return {@link java.util.Optional<DocumentEntity>}.
   * @throws IOException If request to ES failed.
   */
  public Optional<Document> getDocumentById(@NonNull String documentId) throws IOException {
    GetResponse<DocumentEntity> response =
        esClient.get(g -> g.id(documentId), DocumentEntity.class);
    if (response.found() && response.source() != null) {
      return Optional.of(response.source().toApiModel());
    } else {
      return Optional.empty();
    }
  }

  public Page<Document> getDocumentsByName(@NonNull String documentName, Integer page)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    if (documentName.trim().isEmpty()) {
      return getAllDocuments(page);
    }
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page > 0) s.from(page * batchSize).size(batchSize);
              return s.query(
                  q ->
                      q.wildcard(
                          new WildcardQuery.Builder()
                              .field(DocumentFields.TITLE.name())
                              .wildcard(documentName)
                              .caseInsensitive(true)
                              .build()));
            },
            DocumentEntity.class);
    return toPage(response, page);
  }

  public Page<Document> getDocumentsByIds(@NonNull Collection<String> ids, Integer page)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page > 0) s.from(page * batchSize).size(batchSize);
              return s.query(
                  q -> q.ids(new IdsQuery.Builder().values(new ArrayList<>(ids)).build()));
            },
            DocumentEntity.class);
    return toPage(response, page);
  }

  public Page<Document> getDocumentsByPhrases(@NonNull Collection<String> phrases, Integer page)
      throws IOException {
    int batchSize = prepareBatchSize(config.getBatchSize());
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s -> {
              if (page != null && page > 0) s.from(page * batchSize).size(batchSize);
              // TODO: filter by phrases, and/or?
              // could use: SimpleQueryStringQuery.of(q -> q.euery("+John +Doe"))._toQuery();
              return s;
            },
            DocumentEntity.class);
    return toPage(response, page);
  }

  public Page<Document> getDocumentsByIdsAndPhrases(
      @NonNull Collection<String> ids, @NonNull Collection<String> phrases, Integer page) {
    throw new NotImplementedException();
    //    return documentRepository
    //      .findDocumentEntitiesByIdInAndDocumentTextIn(ids, phrases, pageRequestOf(page))
    //      .map(DocumentEntity::toApiModel);
  }

  public Page<Document> getDocumentsByTerms(String[] terms, String[] fields) {
    throw new NotImplementedException();
    //    return documentRepository.getESDocumentsByTerms(terms, fields).stream()
    //      .map(DocumentEntity::toApiModel)
    //      .collect(Collectors.toList());
  }

  // ### method calls for the custom ES repository

  public Page<Document> getDocumentsByTermsBoolean(
      String[] mustTerms, String[] shouldTerms, String[] notTerms, String[] fields) {
    throw new NotImplementedException();
    //    return documentRepository
    //      .getESDocumentsByTermsBoolean(shouldTerms, mustTerms, notTerms, fields)
    //      .stream()
    //      .map(DocumentEntity::toApiModel)
    //      .collect(Collectors.toList());
  }

  public Page<Document> getDocumentsByPhrases(String[] phrases, String[] fields) {
    throw new NotImplementedException();
    //    return documentRepository.getESDocumentsByPhrases(phrases, fields).stream()
    //      .map(DocumentEntity::toApiModel)
    //      .collect(Collectors.toList());
  }

  public Page<Document> getDocumentsByPhrasesBoolean(
      String[] mustPhrases, String[] shouldPhrases, String[] notPhrases, String[] fields) {
    throw new NotImplementedException();
    //    return documentRepository
    //      .getESDocumentsByPhrasesBoolean(shouldPhrases, mustPhrases, notPhrases, fields)
    //      .stream()
    //      .map(DocumentEntity::toApiModel)
    //      .collect(Collectors.toList());
  }

  private int prepareBatchSize(Integer batchSize) {
    return batchSize == null || batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
  }

  private Page<Document> toPage(SearchResponse<DocumentEntity> response, Integer page) {
    List<Document> content =
        response.hits().hits().stream()
            .map(Hit::source)
            .filter(Objects::nonNull)
            .map(DocumentEntity::toApiModel)
            .collect(Collectors.toList());
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
