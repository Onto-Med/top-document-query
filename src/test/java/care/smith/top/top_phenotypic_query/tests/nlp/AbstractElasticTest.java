package care.smith.top.top_phenotypic_query.tests.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import care.smith.top.top_phenotypic_query.song.adapter.Document;
import care.smith.top.top_phenotypic_query.song.adapter.TextAdapter;
import care.smith.top.top_phenotypic_query.song.adapter.lucene.LuceneAdapter;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public abstract class AbstractElasticTest {
  protected static final String ELASTIC_URL = "localhost";
  protected static final String ELASTIC_PORT = "9200";
  protected static final String[] ELASTIC_INDEX = new String[] {"test_documents"};
  protected static final String[] ELASTIC_FIELD = new String[] {"text"};
  protected static ElasticsearchClient esClient;
  protected static TextAdapter adapter;

  /**
   * The ES index "test_documents" should be populated with the following three documents:
   *
   * <ul>
   *   <li>_id: 01, _source: {name: test01, text: What do we have here? A test document. With an
   *       entity. Nice.},
   *   <li>_id: 02, _source: {name: test02, text: Another document is here. It has two entities.},
   *   <li>_id: 03, _source: {name: test03, text: And a third document; but this one features
   *       nothing.}
   * </ul>
   */
  protected static void setUpESIndex() {
    RestClient restClient =
        RestClient.builder(new HttpHost(ELASTIC_URL, Integer.parseInt(ELASTIC_PORT))).build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    esClient = new ElasticsearchClient(transport);
    assertNotNull(esClient);

    try {
      SearchResponse<Document> search =
          esClient.search(
              s ->
                  s.index(Arrays.asList(ELASTIC_INDEX))
                      .query(q -> q.matchAll(v -> v.queryName("matchAll"))),
              Document.class);

      if (search.hits().hits().size() == 0) {
        esClient.index(i -> i
                .id("01")
                .index(ELASTIC_INDEX[0])
                .document(
                        new TextDocument(
                                "test01",
                                "What do we have here? A test document. With an entity. Nice."))
        );
        esClient.index(i -> i
                .id("02")
                .index(ELASTIC_INDEX[0])
                .document(
                        new TextDocument(
                                "test02",
                                "Another document is here. It has two entities."))
        );
        esClient.index(
                i ->
                        i.id("03")
                                .index(ELASTIC_INDEX[0])
                                .document(
                                        new TextDocument(
                                                "test03",
                                                "And a third document; but this one features nothing")));
      } else {assertEquals(3, search.hits().hits().size());}
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
