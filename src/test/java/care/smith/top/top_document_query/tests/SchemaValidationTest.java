package care.smith.top.top_document_query.tests;

import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public class SchemaValidationTest {
  private static final String CONFIG_DIR = "/config/";
  private static final String SCHEMA_FILE = "/text_adapter_config_schema.json";

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidationTest.class);

  @Test
  public void rejectsUnknownMixedAndInvalidV2Settings() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
    Schema schema = registry.getSchema(TextAdapterConfig.class.getResourceAsStream(SCHEMA_FILE));
    String valid =
        "configVersion: 2\nconnection: {endpoints: ['https://localhost:9200']}\nsource: {indices: [one]}\n";
    Assertions.assertTrue(schema.validate(mapper.readTree(valid)).isEmpty());
    for (String invalid :
        List.of(
            valid + "index: [one]\n",
            valid + "search: {fileds: [text]}\n",
            valid.replace("indices: [one]", "indices: []"),
            valid.replace("indices: [one]", "indices: [one, one]"),
            valid.replace("indices: [one]", "indices: [one], batchSize: 0"),
            valid.replace("https://localhost:9200", "localhost:9200"),
            valid.replace("configVersion: 2", "configVersion: 3"))) {
      Assertions.assertFalse(schema.validate(mapper.readTree(invalid)).isEmpty(), invalid);
    }
  }

  @Test
  public void validatesServiceEndpointLists() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
    Schema schema = registry.getSchema(TextAdapterConfig.class.getResourceAsStream(SCHEMA_FILE));
    String base =
        "configVersion: 2\nconnection: {endpoints: ['https://localhost:9200']}\nsource: {indices: [one]}\n";
    for (String service : List.of("conceptGraph", "vectorStore")) {
      for (String endpoints :
          List.of(
              "['https://localhost:5000']",
              "['https://localhost:5000', 'http://localhost:5001', 'https://other']")) {
        Assertions.assertTrue(
            schema
                .validate(
                    mapper.readTree(
                        base + "services: {" + service + ": {endpoints: " + endpoints + "}}\n"))
                .isEmpty());
      }
      for (String settings :
          List.of(
              "{}",
              "{endpoints: null}",
              "{endpoints: []}",
              "{endpoints: ['http://localhost:5000', 'http://localhost:5000']}",
              "{endpoints: ['localhost:5000']}",
              "{endpoints: ['http://localhost/path']}",
              "{endpoints: [' ']}",
              "{endpoint: 'http://localhost:5000'}",
              "{endpoints: ['http://localhost:5000'], endpoint: 'http://localhost:5001'}")) {
        String yaml = base + "services: {" + service + ": " + settings + "}\n";
        Assertions.assertFalse(schema.validate(mapper.readTree(yaml)).isEmpty(), yaml);
      }
    }
  }

  @Test
  public void testSchemataAreValid() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
    InputStream schemaStream = TextAdapterConfig.class.getResourceAsStream(SCHEMA_FILE);
    Schema schema = schemaRegistry.getSchema(schemaStream);

    File configDir = new File(Objects.requireNonNull(getClass().getResource(CONFIG_DIR)).getPath());
    for (File configFile :
        Objects.requireNonNull(
            configDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml")))) {
      LOGGER.debug("Validating config file: {}", configFile.getName());
      List<Error> messages = schema.validate(mapper.readTree(configFile));
      Assertions.assertEquals(Collections.emptyList(), messages);
    }
  }
}
