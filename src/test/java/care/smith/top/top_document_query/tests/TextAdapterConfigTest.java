package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.top_document_query.adapter.config.Connection;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextAdapterConfigTest {
  @TempDir Path directory;

  private TextAdapterConfig load(String yaml) throws IOException {
    Path file = directory.resolve("adapter.yml");
    Files.writeString(file, yaml);
    return TextAdapterConfig.getInstance(file.toString());
  }

  private String v2(String source) {
    return "configVersion: 2\nconnection:\n  endpoints: [https://localhost:9201, http://localhost:9202]\nsource:\n" + source;
  }

  @Test
  void exampleV2LoadsWithLegacyAccessors() throws Exception {
    Path file = Path.of(getClass().getResource("/config/Example_Adapter_v2.yml").toURI());
    TextAdapterConfig config = TextAdapterConfig.getInstance(file.toString());
    assertEquals(2, config.getConfigVersion());
    assertArrayEquals(new String[] {"test_documents"}, config.getIndex());
    assertArrayEquals(new String[] {"text"}, config.getField());
    assertEquals(30, config.getBatchSize());
    assertEquals(Map.of("text", "content"), config.getReplaceFields());
    assertEquals("label", config.getLabelKey());
    assertEquals("id", config.getOtherId());
    assertEquals(List.of("http://localhost:9201", "http://192.168.48.111:9201"), config.getConnection().getEndpoints());
    assertEquals("http://localhost", config.getConceptGraph().getConnection().getUrl());
    assertEquals("5000", config.getConceptGraph().getConnection().getPort());
    assertEquals("8882", config.getVectorStore().getConnection().getPort());
  }

  @Test
  void legacyExamplesStillLoad() throws Exception {
    for (String name : List.of("Example_Adapter.yml", "Example_Adapter_Local.yml")) {
      TextAdapterConfig config = TextAdapterConfig.getInstance(
          Path.of(getClass().getResource("/config/" + name).toURI()).toString());
      assertEquals(1, config.getConfigVersion());
      assertEquals(2, config.getConnection().getEndpoints().size());
    }
  }

  @Test
  void defaultsAndOptionalServices() throws IOException {
    TextAdapterConfig config = load(v2("  indices: [one, two, .hidden]\n"));
    assertArrayEquals(new String[] {"one", "two", ".hidden"}, config.getIndex());
    assertEquals(30, config.getBatchSize());
    assertArrayEquals(new String[] {"text"}, config.getField());
    assertNull(config.getConceptGraph());
    assertNull(config.getVectorStore());
    assertEquals("https://localhost", config.getConnection().getUrl());
    assertEquals("http://localhost", config.getConnection().getAlternateUrl());
    assertEquals("9201", config.getConnection().getPort());
  }

  @Test
  void nestedSettingsOverrideDefaults() throws IOException {
    TextAdapterConfig config = load(v2("  indices: [one]\n  batchSize: 5\n") + """
        search:
          fields: [body, title]
        pipeline:
          documentMapping:
            renameFields: {body: content}
            labelField: category
            idField: external_id
        """);
    assertEquals(5, config.getBatchSize());
    assertArrayEquals(new String[] {"body", "title"}, config.getField());
    assertEquals(Map.of("body", "content"), config.getReplaceFields());
    assertEquals("category", config.getLabelKey());
    assertEquals("external_id", config.getOtherId());
  }

  @Test
  void malformedAmbiguousAndUnknownSettingsFailFast() {
    for (String yaml : List.of(
        "configVersion: 3\n",
        "configVersion: 2.5\n",
        "[]",
        v2("  indices: []\n"),
        v2("  indices: [one, one]\n"),
        v2("  indices: [One]\n"),
        v2("  indices: ['*']\n"),
        v2("  indices: ['.']\n"),
        v2("  indices: ['..']\n"),
        v2("  indices: [one]\n  batchSize: 0\n"),
        v2("  indices: [one]\n") + "field: [text]\n",
        v2("  indices: [one]\n") + "search: {fileds: [text]}\n",
        v2("  indices: [one]\n") + "id: one\nid: two\n",
        v2("  indices: [one]\n") + "services: {conceptGraph: {}}\n",
        "configVersion: 2\nconnection: {url: 'http://localhost', endpoints: ['http://localhost:9200']}\nsource: {indices: [one]}\n",
        "connection: {endpoints: ['http://localhost:9200']}\n")) {
      assertThrows(IllegalArgumentException.class, () -> load(yaml), yaml);
    }
    assertThrows(IllegalArgumentException.class,
        () -> TextAdapterConfig.getInstance(directory.resolve("missing.yml").toString()));
  }

  @Test
  void endpointValidationPreservesSchemeAndPerEndpointPorts() {
    Connection connection = new Connection();
    connection.setEndpoints(List.of("https://localhost", "http://localhost:9202", "http://[::1]:9203"));
    connection.validate();
    assertEquals("443", connection.getPort());
    assertEquals("http://[::1]:9203", connection.getEndpoints().getLast());
    assertThrows(UnsupportedOperationException.class, () -> connection.getEndpoints().add("http://other"));
    for (String endpoint : List.of("localhost:9200", "ftp://localhost", "https://localhost:0",
        "https://localhost:65536", "https://localhost/path", "https://user:password@localhost",
        "https://localhost?q=x", "https://localhost#fragment")) {
      assertThrows(IllegalArgumentException.class, () -> Connection.validateEndpoint(endpoint));
    }
  }
}
