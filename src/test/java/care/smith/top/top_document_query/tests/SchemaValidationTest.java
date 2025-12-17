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
  public void testSchemataAreValid() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
    InputStream schemaStream = TextAdapterConfig.class.getResourceAsStream(SCHEMA_FILE);
    Schema schema = schemaRegistry.getSchema(schemaStream);

    File configDir = new File(Objects.requireNonNull(getClass().getResource(CONFIG_DIR)).getPath());
    for (File configFile : Objects.requireNonNull(configDir.listFiles())) {
      LOGGER.debug("Validating config file: {}", configFile.getName());
      List<Error> messages = schema.validate(mapper.readTree(configFile));
      Assertions.assertEquals(Collections.emptyList(), messages);
    }
  }
}
