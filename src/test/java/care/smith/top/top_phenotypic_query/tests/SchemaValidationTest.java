package care.smith.top.top_phenotypic_query.tests;

import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaValidationTest {
  private static final String CONFIG_DIR = "/config/";
  private static final String SCHEMA_FILE = "/adapter_config_schema.json";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Test
  public void testSchemataAreValid() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    JsonSchemaFactory factory =
        JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7))
            .objectMapper(mapper)
            .build();

    InputStream schemaStream = DataAdapter.class.getResourceAsStream(SCHEMA_FILE);
    JsonSchema schema = factory.getSchema(schemaStream);

    File configDir = new File(Objects.requireNonNull(getClass().getResource(CONFIG_DIR)).getPath());
    for (File configFile : Objects.requireNonNull(configDir.listFiles())) {
      logger.debug("Validating config file: {}", configFile.getName());
      Set<ValidationMessage> messages = schema.validate(mapper.readTree(configFile));
      Assertions.assertEquals(Collections.emptySet(), messages);
    }
  }
}
