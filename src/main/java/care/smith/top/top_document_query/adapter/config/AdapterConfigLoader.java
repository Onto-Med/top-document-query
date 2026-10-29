package care.smith.top.top_document_query.adapter.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Loads either legacy or v2 YAML into the adapter's backwards-compatible Java configuration. */
final class AdapterConfigLoader {
  private AdapterConfigLoader() {}

  static TextAdapterConfig load(Path path) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    try {
      JsonNode root = mapper.readTree(path.toFile());
      if (root == null || !root.isObject()) {
        throw new IllegalArgumentException("Adapter configuration must be an object");
      }
      JsonNode version = root.get("configVersion");
      if (version != null && (!version.isIntegralNumber() || !version.canConvertToInt())) {
        throw new IllegalArgumentException("configVersion must be an integer");
      }
      int number = version == null ? 1 : version.intValue();
      TextAdapterConfig config;
      if (number == 1) {
        config = mapper.treeToValue(root, TextAdapterConfig.class);
        if (config.getConnection() != null && config.getConnection().hasExplicitEndpoints()) {
          throw new IllegalArgumentException("connection.endpoints requires configVersion: 2");
        }
      } else if (number == 2) {
        config = fromV2(mapper.treeToValue(root, V2.class));
      } else {
        throw new IllegalArgumentException("Unsupported configVersion: " + number);
      }
      config.validate();
      return config;
    } catch (IOException e) {
      // Do not log parser excerpts: connection/configuration files may contain sensitive values.
      throw new IllegalArgumentException("Could not read adapter configuration: " + path);
    }
  }

  private static TextAdapterConfig fromV2(V2 value) {
    if (value.connection() == null || !value.connection().hasExplicitEndpoints()) {
      throw new IllegalArgumentException("v2 requires connection.endpoints");
    }
    if (value.connection().hasLegacyAddress()) {
      throw new IllegalArgumentException("Do not mix connection.endpoints with legacy URL/port keys");
    }
    if (value.source() == null || value.source().indices() == null) {
      throw new IllegalArgumentException("v2 requires source.indices");
    }
    TextAdapterConfig config = new TextAdapterConfig();
    config.setConfigVersion(2);
    if (value.id() != null) config.setId(value.id());
    if (value.adapter() != null) config.setAdapter(value.adapter());
    config.setConnection(value.connection());
    config.setIndex(value.source().indices().toArray(String[]::new));
    if (value.source().batchSize() != null) config.setBatchSize(value.source().batchSize());
    if (value.search() != null && value.search().fields() != null) {
      config.setField(value.search().fields().toArray(String[]::new));
    }
    if (value.pipeline() != null && value.pipeline().documentMapping() != null) {
      DocumentMapping mapping = value.pipeline().documentMapping();
      if (mapping.renameFields() != null) config.setReplaceFields(mapping.renameFields());
      if (mapping.labelField() != null) config.setLabelKey(mapping.labelField());
      if (mapping.idField() != null) config.setOtherId(mapping.idField());
    }
    if (value.services() != null) {
      if (value.services().conceptGraph() != null) {
        ConceptGraphConfig service = new ConceptGraphConfig();
        service.setConnection(serviceConnection(value.services().conceptGraph()));
        config.setConceptGraph(service);
      }
      if (value.services().vectorStore() != null) {
        VectorStoreConfig service = new VectorStoreConfig();
        service.setConnection(serviceConnection(value.services().vectorStore()));
        config.setVectorStore(service);
      }
    }
    return config;
  }

  private static Connection serviceConnection(Service service) {
    Connection connection = new Connection();
    connection.setEndpoints(service.endpoints());
    return connection;
  }

  // Separate input types deliberately reject mixed legacy/v2 keys and unknown properties.
  record V2(
      Integer configVersion,
      String id,
      String adapter,
      Connection connection,
      Source source,
      Search search,
      Pipeline pipeline,
      Services services) {}

  record Source(List<String> indices, Integer batchSize) {}

  record Search(List<String> fields) {}

  record Pipeline(DocumentMapping documentMapping) {}

  record DocumentMapping(Map<String, String> renameFields, String labelField, String idField) {}

  record Services(Service conceptGraph, Service vectorStore) {}

  record Service(List<String> endpoints) {}
}
