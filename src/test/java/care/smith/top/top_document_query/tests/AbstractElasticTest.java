package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.lucene.LuceneAdapter;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public abstract class AbstractElasticTest {
  protected static final String[] ELASTIC_INDEX = new String[] {"test_documents"};
  protected static DocumentElasticsearchContainer elasticsearchContainer =
      new DocumentElasticsearchContainer();
  protected static ElasticsearchClient esClient;
  protected static TextAdapter adapter;
  protected static Map<String, String> documents =
      Map.of(
          "test01", "What do we have here? A test document. With an entity. Nice.",
          "test02", "Another document is here. It has two entities.",
          "test03", "And a third document; but this one features nothing");

  protected static void setUpESIndex() {
    elasticsearchContainer.start();
    RestClient restClient =
        RestClient.builder(HttpHost.create(elasticsearchContainer.getHttpHostAddress())).build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    esClient = new ElasticsearchClient(transport);
    assertNotNull(esClient);

    try {
      esClient.index(i -> i
              .id("01")
              .index(ELASTIC_INDEX[0])
              .document( new TextDocument("test01", documents.get("test01")))
      );
      esClient.index(i -> i
              .id("02")
              .index(ELASTIC_INDEX[0])
              .document( new TextDocument("test02", documents.get("test02")))
      );
      esClient.index(i -> i
              .id("03")
              .index(ELASTIC_INDEX[0])
              .document( new TextDocument("test03", documents.get("test03")))
      );
      await().until(() -> esClient.count().count() == 3);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static void initAdaper() throws InstantiationException {
    URL configFile =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource("config/Elastic_Adapter_Test.yml");
    assertNotNull(configFile);

    adapter = LuceneAdapter.getInstance(configFile.getPath());
    assertNotNull(adapter);
  }

  static class TextDocument {
    public String name;
    public String text;
    public TextDocument(String name, String text) {
      this.name = name;
      this.text = text;
    }
  }
}
