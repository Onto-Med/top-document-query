package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.*;
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
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
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
    return Stream.generate(
            new Supplier<List<DocumentHit>>() {
              List<FieldValue> sortValues = List.of(FieldValue.of(""));
              // ToDo: need to check the value for "name.keyword"
              final FieldSort fs =
                  new FieldSort.Builder().field("name.keyword").order(SortOrder.Asc).build();
              final int bs = prepareBatchSize(config.getBatchSize());

              @Override
              public List<DocumentHit> get() {
                try {
                  List<Hit<DocumentEntity>> hits =
                      getSearchAfter(query, highlight, fs, bs, sortValues).hits().hits();
                  sortValues = getLastSortValues(hits);
                  return hits.stream()
                      .map(
                          hit ->
                              new DocumentHit(
                                  hit.id(),
                                  hit.source(),
                                  exactHighlight
                                      ? mergeHighlights(hit.highlight(), queryString)
                                      : hit.highlight(),
                                  hit.score()))
                      .toList();
                } catch (IOException e) {
                  LOGGER.fine(
                      String.format(
                          "Could not retrieve documents for query:\n'%s'",
                          highlight.highlightQuery()));
                  return List.of();
                }
              }
            })
        .takeWhile(list -> !list.isEmpty());
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
            .split("\\s+");
    HashMap<String, List<String>> mergedHighlights = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
      ArrayList<String> newHighlights = new ArrayList<>();
      for (String highlight : entry.getValue()) {
        StringBuilder highlightBuilder = new StringBuilder(highlight);
        Pattern pattern =
            Pattern.compile(
                Arrays.stream(queryComponents)
                    .map(s -> String.format("<em>%s</em>", s))
                    .collect(Collectors.joining("(\\s+)")));
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
    return getDocumentsByIdsPaged(List.of(documentId), 0, simplified).stream().findFirst();
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

    @Override
    public DocumentImport importDocuments(@NonNull Document[] documents, String indexName, String language) throws IOException {
        if (initDocumentIndex(indexName, language)) {
            System.out.println(esClient.indices().getMapping());
        }
        return null;
    }

    private boolean initDocumentIndex(String indexName, String language) {
        if (hasIndex(indexName)) {
            LOGGER.warning("Index already exists: " + indexName);
            return true;
        }
        try {
            esClient.indices().create(c -> c
                    .index(indexName)
                    .mappings(m -> m
                                    .properties((Map<String, Property>) ElasticsearchIndexSettings.getMappings(language).get("properties"))
                            )
//                    .settings()
            );
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

    private SearchResponse<DocumentEntity> getSearchAfter(
      Query query,
      @Nullable Highlight highlight,
      FieldSort fieldSort,
      Integer batchSize,
      List<FieldValue> sortValues)
      throws IOException {
    List<FieldValue> finalSortValues = sortValues;
    SearchResponse<DocumentEntity> response =
        esClient.search(
            s ->
                s.index(Arrays.asList(config.getIndex()))
                    .query(query)
                    .highlight(highlight)
                    .sort(sb -> sb.field(fieldSort))
                    .size(batchSize)
                    .searchAfter(finalSortValues),
            DocumentEntity.class);
    sortValues = getLastSortValues(response.hits().hits());
    return response;
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
          List<Hit<DocumentEntity>> hits =
              getSearchAfter(query, null, fs, bs, sortValues).hits().hits();
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
    FieldValue documentName =
        (lastHit.source() != null) ? FieldValue.of(lastHit.source().getName()) : FieldValue.NULL;
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
    String alternateHost = null;

    try {
      URL url = new URL(config.getConnection().getUrl());
      host = url.getHost();
    } catch (MalformedURLException e) {
      host = config.getConnection().getUrl();
    }

    if (config.getConnection().getAlternateUrl() != null) {
      try {
        URL url = new URL(config.getConnection().getAlternateUrl());
        alternateHost = url.getHost();

      } catch (MalformedURLException e) {
        alternateHost = config.getConnection().getAlternateUrl();
      }
    }

    RestClient restClient =
        RestClient.builder(new HttpHost(host, Integer.parseInt(config.getConnection().getPort())))
            .build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    this.esClient = new ElasticsearchClient(transport);
    try {
      // simple call to esClient to test whether the connection works
      String luceneVersion = this.esClient.info().version().luceneVersion();
    } catch (IOException e) {
      if (alternateHost == null) {
        LOGGER.severe(
            "Could not connect to Elasticsearch at '" + host + "'. Alternate URL not set.");
      } else {
        LOGGER.warning(
            "Could not connect to Elasticsearch at "
                + host
                + ". Trying alternate URL at '"
                + alternateHost
                + "'.");
        RestClient alternateRestClient =
            RestClient.builder(
                    new HttpHost(alternateHost, Integer.parseInt(config.getConnection().getPort())))
                .build();
        ElasticsearchTransport alternateTransport =
            new RestClientTransport(alternateRestClient, new JacksonJsonpMapper());
        this.esClient = new ElasticsearchClient(alternateTransport);
      }
    }
  }
}
