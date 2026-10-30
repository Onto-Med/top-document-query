package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.DocumentReference;
import care.smith.top.top_document_query.adapter.config.Connection;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.transport.JsonEndpoint;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;

/** Captures Elasticsearch requests without opening sockets or starting containers. */
class ElasticsearchIndexScopeTest {
  private TextAdapterConfig config() {
    TextAdapterConfig config = new TextAdapterConfig();
    config.setConfigVersion(2);
    config.setIndex(new String[] {"one", "two"});
    config.setBatchSize(1);
    Connection connection = new Connection();
    connection.setEndpoints(List.of("https://localhost:9200"));
    config.setConnection(connection);
    return config;
  }

  private ElasticsearchAdapter adapter(TextAdapterConfig config, RecordingTransport transport) {
    return new ElasticsearchAdapter(config, new ElasticsearchClient(transport));
  }

  @Test
  void scopesAreIndependentAndCannotExpandOrEscapeTheAllowlist() throws Exception {
    RecordingTransport transport = new RecordingTransport();
    ElasticsearchAdapter root = adapter(config(), transport);
    ElasticsearchAdapter one = root.forIndex("one");
    ElasticsearchAdapter two = root.forIndex("two");
    assertEquals(List.of("one"), one.getSelectedIndices());
    assertEquals(List.of("two"), two.getSelectedIndices());
    assertArrayEquals(new String[] {"one", "two"}, root.getConfig().getIndex());
    assertThrows(IllegalStateException.class, root::count);
    assertThrows(IllegalArgumentException.class, () -> one.forIndex("two"));
    assertThrows(IllegalArgumentException.class, () -> root.forIndex("outside"));
    assertThrows(IllegalStateException.class, () -> root.importDocuments(new Document[0], "en"));
    assertTrue(transport.requests.isEmpty());
    one.close();
    assertFalse(transport.closed, "Closing a view must not close the shared client");
  }

  @Test
  void pagedReadsUseTheSelectedIndex() throws Exception {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(search(List.of(hit("two", "same", 1))));
    adapter(config(), transport).forIndex("two").getAllDocumentsPaged(0, false);
    SearchRequest request = (SearchRequest) transport.requests.getFirst();
    assertEquals(List.of("two"), request.index());
  }

  @Test
  void indexedPagesRetainIdentityWhenIdsCollide() throws Exception {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(search(List.of(hit("one", "same", 1), hit("two", "same", 2))));
    var page =
        adapter(config(), transport)
            .forIndices(List.of("one", "two"))
            .getIndexedDocumentsByIdsPaged(List.of("same"), 0, false);
    assertEquals(List.of("one", "two"), page.stream().map(d -> d.reference().index()).toList());
    assertNotEquals(page.getContent().get(0).reference(), page.getContent().get(1).reference());
    assertEquals(List.of("one", "two"), ((SearchRequest) transport.requests.getFirst()).index());
  }

  @Test
  void importCreatesAndWritesTheExplicitDestinationOnly() {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(new BooleanResponse(false));
    transport.responses.add(
        CreateIndexResponse.of(r -> r.index("two").acknowledged(true).shardsAcknowledged(true)));
    transport.responses.add(
        BulkResponse.of(
            r ->
                r.errors(false)
                    .took(1)
                    .items(
                        i ->
                            i.operationType(
                                    co.elastic.clients.elasticsearch.core.bulk.OperationType.Index)
                                .index("two")
                                .id("id")
                                .status(201))));
    ElasticsearchAdapter root = adapter(config(), transport);
    assertEquals(
        1,
        root.importDocuments(
                "two", new Document[] {new Document().id("id").name("name").text("text")}, "en")
            .getCount());
    assertEquals(List.of("two"), ((ExistsRequest) transport.requests.get(0)).index());
    assertEquals("two", ((CreateIndexRequest) transport.requests.get(1)).index());
    assertEquals(
        "two", ((BulkRequest) transport.requests.get(2)).operations().getFirst().index().index());
    assertArrayEquals(new String[] {"one", "two"}, root.getConfig().getIndex());
  }

  @Test
  void hitProvenanceAndServerSortValuesSurviveMultiIndexBatching() {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(openPit());
    transport.responses.add(search(List.of(hit("one", "same", 1))));
    transport.responses.add(search(List.of(hit("two", "same", 2))));
    transport.responses.add(search(List.of()));
    transport.responses.add(ClosePointInTimeResponse.of(r -> r.succeeded(true).numFreed(1)));
    ElasticsearchAdapter root = adapter(config(), transport);
    List<DocumentHit> hits;
    try (Stream<List<DocumentHit>> stream =
        root.forIndices(List.of("one", "two")).execute("text")) {
      hits = stream.flatMap(List::stream).toList();
    }
    assertEquals(List.of("one", "two"), hits.stream().map(DocumentHit::getIndex).toList());
    assertNotEquals(hits.get(0).getReference(), hits.get(1).getReference());
    assertEquals(
        List.of("one", "two"), ((OpenPointInTimeRequest) transport.requests.getFirst()).index());
    SearchRequest first = (SearchRequest) transport.requests.get(1);
    SearchRequest second = (SearchRequest) transport.requests.get(2);
    assertTrue(first.searchAfter().isEmpty());
    var firstJson = JsonData.of(first, transport.jsonpMapper()).toJson().asJsonObject();
    var secondJson = JsonData.of(second, transport.jsonpMapper()).toJson().asJsonObject();
    assertFalse(firstJson.containsKey("search_after"), "First request must omit search_after");
    var searchAfter = secondJson.getJsonArray("search_after");
    assertEquals(2, searchAfter.size());
    assertEquals("same-name", searchAfter.getString(0));
    assertEquals(1L, searchAfter.getJsonNumber(1).longValueExact());
    assertEquals("same-name", second.searchAfter().getFirst().stringValue());
    assertEquals(1L, second.searchAfter().get(1).longValue());
    assertEquals("_shard_doc", first.sort().get(1).field().field());
    assertEquals("pit", first.pit().id());
    assertInstanceOf(ClosePointInTimeRequest.class, transport.requests.getLast());
  }

  @Test
  void openingPitUsesStandardClientEndpointAndPreservesRoutingAndOptions() {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(openPit());
    transport.responses.add(search(List.of(hit("one", "id", 1))));
    transport.responses.add(ClosePointInTimeResponse.of(r -> r.succeeded(true).numFreed(1)));
    TransportOptions options =
        transport.options().toBuilder().addHeader("X-Test", "pit-options").build();
    ElasticsearchClient client = new ElasticsearchClient(transport).withTransportOptions(options);
    try (Stream<List<Document>> stream =
        new ElasticsearchAdapter(config(), client)
            .forIndex("one")
            .getAllDocumentsBatched(1, false)) {
      assertEquals(1, stream.limit(1).count());
    }
    PitRequestDetails pit = transport.pitRequests.getFirst();
    assertSame(OpenPointInTimeRequest._ENDPOINT, pit.endpoint());
    assertEquals("POST", pit.method());
    assertEquals("/one/_pit", pit.path());
    assertEquals(Map.of("keep_alive", "1m"), pit.parameters());
    assertSame(client._transportOptions(), pit.options());
  }

  @Test
  void closingShortCircuitedStreamReleasesItsPit() {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(openPit());
    transport.responses.add(search(List.of(hit("one", "id", 1))));
    transport.responses.add(ClosePointInTimeResponse.of(r -> r.succeeded(true).numFreed(1)));
    try (Stream<List<Document>> stream =
        adapter(config(), transport).forIndex("one").getAllDocumentsBatched(1, false)) {
      assertEquals(1, stream.limit(1).count());
    }
    assertInstanceOf(ClosePointInTimeRequest.class, transport.requests.getLast());
  }

  @Test
  void failedSearchReleasesPitAndDoesNotReturnPartialSuccess() {
    RecordingTransport transport = new RecordingTransport();
    transport.responses.add(openPit());
    transport.responses.add(new IOException("simulated"));
    transport.responses.add(ClosePointInTimeResponse.of(r -> r.succeeded(true).numFreed(1)));
    try (Stream<List<DocumentHit>> stream =
        adapter(config(), transport).forIndex("one").execute("text")) {
      assertThrows(UncheckedIOException.class, () -> stream.toList());
    }
    assertInstanceOf(ClosePointInTimeRequest.class, transport.requests.getLast());
  }

  @Test
  void exactReferenceCanResolveConcreteIndexThroughConfiguredAlias() throws Exception {
    RecordingTransport transport = new RecordingTransport();
    TextAdapterConfig config = config();
    config.setIndex(new String[] {"alias"});
    transport.responses.add(search(List.of(hit("actual-index", "same", 1))));
    ElasticsearchAdapter root = adapter(config, transport);
    assertTrue(
        root.getDocument(new DocumentReference(config.getId(), "actual-index", "same"), false)
            .isPresent());
    SearchRequest request = (SearchRequest) transport.requests.getFirst();
    assertEquals(List.of("alias"), request.index());
    assertEquals("_index", request.query().bool().filter().get(1).term().field());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            root.getDocument(new DocumentReference("other-source", "actual-index", "same"), false));
  }

  @Test
  void idOnlyLookupRequiresOneIndexAndRejectsAliasCollisions() {
    RecordingTransport transport = new RecordingTransport();
    ElasticsearchAdapter root = adapter(config(), transport);
    assertThrows(
        IllegalStateException.class,
        () -> root.forIndices(List.of("one", "two")).getDocumentById("same", false));
    transport.responses.add(search(List.of(hit("one", "same", 1), hit("two", "same", 2))));
    assertThrows(
        IllegalStateException.class, () -> root.forIndex("one").getDocumentById("same", false));
  }

  private String openPit() {
    return "{\"id\":\"pit\",\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0}}";
  }

  private Hit<DocumentEntity> hit(String index, String id, long shardSort) {
    return Hit.of(
        h ->
            h.index(index)
                .id(id)
                .source(new DocumentEntity().setId(id).setName("same-name").setText("text"))
                .sort(List.of(FieldValue.of("same-name"), FieldValue.of(shardSort))));
  }

  private SearchResponse<DocumentEntity> search(List<Hit<DocumentEntity>> hits) {
    return SearchResponse.of(
        r ->
            r.took(1)
                .timedOut(false)
                .shards(s -> s.total(1).successful(1).failed(0))
                .hits(
                    h ->
                        h.hits(hits)
                            .total(
                                t ->
                                    t.value(hits.size())
                                        .relation(
                                            co.elastic.clients.elasticsearch.core.search
                                                .TotalHitsRelation.Eq))));
  }

  private record PitRequestDetails(
      Endpoint<?, ?, ?> endpoint,
      String method,
      String path,
      Map<String, String> parameters,
      TransportOptions options) {}

  private static final class RecordingTransport implements ElasticsearchTransport {
    final List<Object> requests = new ArrayList<>();
    final List<PitRequestDetails> pitRequests = new ArrayList<>();
    final ArrayDeque<Object> responses = new ArrayDeque<>();
    boolean closed;

    @Override
    @SuppressWarnings("unchecked")
    public <RequestT, ResponseT, ErrorT> ResponseT performRequest(
        RequestT request, Endpoint<RequestT, ResponseT, ErrorT> endpoint, TransportOptions options)
        throws IOException {
      requests.add(request);
      if (request instanceof OpenPointInTimeRequest) {
        pitRequests.add(
            new PitRequestDetails(
                endpoint,
                endpoint.method(request),
                endpoint.requestUrl(request),
                endpoint.queryParameters(request),
                options));
      }
      Object response = responses.removeFirst();
      if (response instanceof IOException error) throw error;
      // Exercise the standard client's PIT response decoder using the aligned server response.
      if (request instanceof OpenPointInTimeRequest && response instanceof String json) {
        JsonpMapper mapper = jsonpMapper();
        try (var parser = mapper.jsonProvider().createParser(new StringReader(json))) {
          return ((JsonEndpoint<RequestT, ResponseT, ErrorT>) endpoint)
              .responseDeserializer()
              .deserialize(parser, mapper);
        }
      }
      return (ResponseT) response;
    }

    @Override
    public <RequestT, ResponseT, ErrorT> CompletableFuture<ResponseT> performRequestAsync(
        RequestT request,
        Endpoint<RequestT, ResponseT, ErrorT> endpoint,
        TransportOptions options) {
      throw new UnsupportedOperationException();
    }

    @Override
    public JsonpMapper jsonpMapper() {
      return new JacksonJsonpMapper();
    }

    @Override
    public TransportOptions options() {
      return new RestClientOptions(RequestOptions.DEFAULT, false);
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
