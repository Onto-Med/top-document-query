package care.smith.top.top_phenotypic_query.song.adapter.lucene;

import care.smith.top.model.ConceptQuery;
import care.smith.top.top_phenotypic_query.song.adapter.Document;
import care.smith.top.top_phenotypic_query.song.adapter.TextAdapter;
import care.smith.top.top_phenotypic_query.song.adapter.TextAdapterConfig;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

// ToDo: rename this with ElasticsearchAdapter to be more precise
public class LuceneAdapter extends TextAdapter {

  private ElasticsearchClient esClient;

  public LuceneAdapter(TextAdapterConfig config) {
    super(config);
    initConnection();
  }

  public LuceneAdapter(String configFile) {
    super(configFile);
    initConnection();
  }

  // ToDo: implement https protocol possibilities
  private void initConnection() {
    String protocol = "http";
    String host;

    try {
      URL url = new URL(config.getConnectionAttribute("url"));
      protocol = url.getProtocol();
      host = url.getHost();
    } catch (MalformedURLException e) {
      host = config.getConnectionAttribute("url");
    }

    RestClient restClient;
    if (Objects.equals(protocol, "http")) {
      restClient =
          RestClient.builder(
                  new HttpHost(host, Integer.parseInt(config.getConnectionAttribute("port"))))
              .build();
    } else {
      throw new NotImplementedException("only http supported at the moment");
    }

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    this.esClient = new ElasticsearchClient(transport);
  }

  @Override
  public List<Document> execute(ConceptQuery query, Entities entities) {

    String queryString =
        Expressions.getStringValue(
            LuceneSong.get()
                .concepts(
                    entities) // ToDo: this needs to be added; how do I get the Concept Entities
                .lang("en") // ToDo: do I get this from query?
                .generate(query.getEntityId()));
    // execute query and return resulting documents
    return execute(queryString);
  }

  @Override
  public List<Document> execute(String queryString) {
    SearchResponse<Document> searchResponse;
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
              Document.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
  }
}
