package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Disabled;

public abstract class AbstractElasticTest {
  protected static final String[] ELASTIC_INDEX = new String[] {"test_documents"};
  protected static DocumentElasticsearchContainer elasticsearchContainer =
      new DocumentElasticsearchContainer();
  protected static ElasticsearchClient esClient;
  protected static ElasticsearchAdapter adapter;
  protected static Document document1 = new Document().id("d1").name("test01")
      .text("What do we have here? A test document. With an entity. Nice.")
      .highlightedText("What do we have here? A test document. With an entity. Nice.");
  protected static Document document2 = new Document().id("d2").name("test02")
      .text("Another document is here. It has two entities.")
      .highlightedText("Another document is here. It has two entities.");
  protected static Document document3 = new Document().id("d3").name("test03")
      .text("And a third document; but this one features nothing. No test.")
      .highlightedText("And a third document; but this one features nothing. No test.");
  protected static Set<Document> allTestDocuments = Set.of(document1, document2, document3);

  protected static void setUpLocalESIndex() {
    RestClient restClient = RestClient.builder(HttpHost.create("http://localhost:9008")).build();
    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());
    esClient = new ElasticsearchClient(transport);
  }

  protected static void deleteIndex() throws IOException {
    AcknowledgedResponse deleteIndexResponse =
        esClient.indices().delete(new DeleteIndexRequest.Builder().index(Arrays.asList(ELASTIC_INDEX)).build());
    await().until(deleteIndexResponse::acknowledged);
  }

  protected static void setUpESIndex() {
    elasticsearchContainer.start();
    RestClient restClient =
        RestClient.builder(HttpHost.create(elasticsearchContainer.getHttpHostAddress())).build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    esClient = new ElasticsearchClient(transport);
    assertNotNull(esClient);

    try {
      esClient.indices().create(ti -> ti.index(ELASTIC_INDEX[0]));
      esClient.indices()
          .putMapping(pm ->
              pm
                .index(Arrays.asList(ELASTIC_INDEX))
                .properties("name", p -> p.text(tp -> tp))
                .properties("name", p -> p.keyword(kp -> kp)));
      esClient.index(
          i ->
              i.id(document1.getId())
                  .index(ELASTIC_INDEX[0])
                  .document(new TextDocument(document1.getId(), document1.getName(), document1.getText())));
      esClient.index(
          i ->
              i.id(document2.getId())
                  .index(ELASTIC_INDEX[0])
                  .document(new TextDocument(document2.getId(), document2.getName(), document2.getText())));
      esClient.index(
          i ->
              i.id(document3.getId())
                  .index(ELASTIC_INDEX[0])
                  .document(new TextDocument(document3.getId(), document3.getName(), document3.getText())));
      await().until(() -> esClient.count().count() == 3);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static void initLocalAdaper() throws InstantiationException {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/Example_Adapter_Local.yml");
    assertNotNull(configFile);

    adapter = (ElasticsearchAdapter) ElasticsearchAdapter.getInstance(configFile.getPath());
    assertNotNull(adapter);
  }

  protected static void initAdaper() throws InstantiationException {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/Example_Adapter.yml");
    assertNotNull(configFile);

    adapter = (ElasticsearchAdapter) ElasticsearchAdapter.getInstance(configFile.getPath());
    assertNotNull(adapter);
  }

  static class TextDocument {
    public String id;
    public String name;
    public String text;

    public TextDocument(String name, String text) {
      this.id = null;
      this.name = name;
      this.text = text;
    }
    public TextDocument(String id, String name, String text) {
      this.id = id;
      this.name = name;
      this.text = text;
    }
  }
}
