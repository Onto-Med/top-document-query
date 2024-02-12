package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.ConceptQuery;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.ElasticDocument;
import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticsearchAdapter extends TextAdapter {

  private ElasticsearchClient esClient;

  public ElasticsearchAdapter(TextAdapterConfig config) {
    super(config);
    initConnection();
  }

  public ElasticsearchAdapter(String configFile) {
    super(configFile);
    initConnection();
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
}
