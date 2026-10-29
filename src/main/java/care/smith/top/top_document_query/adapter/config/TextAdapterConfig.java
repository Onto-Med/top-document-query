package care.smith.top.top_document_query.adapter.config;

import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TextAdapterConfig {
  private int configVersion = 1;

  @JsonSetter(nulls = Nulls.SKIP)
  private String id = "Default Adapter";

  @JsonSetter(nulls = Nulls.SKIP)
  private String adapter = ElasticsearchAdapter.class.getName();

  private Connection connection;

  @JsonSetter(nulls = Nulls.SKIP)
  private String dateField;

  @JsonSetter(nulls = Nulls.SKIP)
  private String[] index = new String[] {"documents"};

  @JsonSetter(nulls = Nulls.SKIP)
  private String[] field = new String[] {"text"};

  @JsonSetter(nulls = Nulls.SKIP)
  private Integer batchSize = 30;

  @JsonSetter(nulls = Nulls.SKIP)
  private String labelKey = "label";

  @JsonSetter(nulls = Nulls.SKIP)
  private String otherId = "id";

  @JsonSetter(nulls = Nulls.SKIP)
  private Map<String, String> replaceFields =
      new HashMap<>() {
        {
          put("text", "content");
        }
      };

  private ConceptGraphConfig conceptGraph;

  private VectorStoreConfig vectorStore;

  public static TextAdapterConfig getInstance(String yamlFilePath) {
    return AdapterConfigLoader.load(Path.of(yamlFilePath));
  }

  public int getConfigVersion() {
    return configVersion;
  }

  public void setConfigVersion(int configVersion) {
    this.configVersion = configVersion;
  }

  /** Validate both file-loaded and programmatically constructed configurations. */
  public void validate() {
    if (configVersion != 1 && configVersion != 2) {
      throw new IllegalArgumentException("Unsupported configVersion");
    }
    if (id == null || id.isBlank() || adapter == null || adapter.isBlank()) {
      throw new IllegalArgumentException("Adapter id and class must not be blank");
    }
    if (connection == null) throw new IllegalArgumentException("connection is required");
    connection.validate();
    validateNames(index, "source indices");
    validateNames(field, "search fields");
    if (Arrays.stream(index).distinct().count() != index.length) {
      throw new IllegalArgumentException("Source indices must be unique");
    }
    if (configVersion == 2) {
      for (String name : index) {
        if (!name.matches("[a-z0-9.][a-z0-9._-]*") || name.equals(".") || name.equals("..")
            || name.length() > 255) {
          throw new IllegalArgumentException("v2 source.indices must contain concrete lowercase index or alias names");
        }
      }
    }
    if (batchSize == null || batchSize <= 0) {
      throw new IllegalArgumentException("batchSize must be positive");
    }
    if (replaceFields == null
        || replaceFields.entrySet().stream().anyMatch(e -> e.getKey() == null
            || e.getKey().isBlank() || e.getValue() == null || e.getValue().isBlank())) {
      throw new IllegalArgumentException("Field mappings must contain nonblank names");
    }
    if (labelKey == null || labelKey.isBlank() || otherId == null || otherId.isBlank()) {
      throw new IllegalArgumentException("Label and ID field names must not be blank");
    }
    if (conceptGraph != null) {
      if (conceptGraph.getConnection() == null) throw new IllegalArgumentException("conceptGraph connection is required");
      conceptGraph.getConnection().validate();
    }
    if (vectorStore != null) {
      if (vectorStore.getConnection() == null) throw new IllegalArgumentException("vectorStore connection is required");
      vectorStore.getConnection().validate();
    }
  }

  private static void validateNames(String[] names, String description) {
    if (names == null || names.length == 0
        || Arrays.stream(names).anyMatch(name -> name == null || name.isBlank())) {
      throw new IllegalArgumentException(description + " must not be empty or contain blank names");
    }
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  public Map<String, String> getReplaceFields() {
    return replaceFields;
  }

  public String getReplaceFieldsAsString() {
    return replaceFields.keySet().stream()
        .map(key -> key + ": " + replaceFields.get(key))
        .collect(Collectors.joining(", ", "{", "}"));
  }

  public String getLabelKey() {
    return labelKey;
  }

  public void setLabelKey(String labelKey) {
    this.labelKey = labelKey;
  }

  public void setReplaceFields(Map<String, String> replaceFields) {
    this.replaceFields = replaceFields;
  }

  public String getAdapter() {
    return adapter;
  }

  public void setAdapter(String adapter) {
    this.adapter = adapter;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOtherId() {
    return otherId;
  }

  public void setOtherId(String otherId) {
    this.otherId = otherId;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public String getDateField() {
    return dateField;
  }

  public void setDateField(String dateField) {
    this.dateField = dateField;
  }

  public String[] getIndex() {
    return index;
  }

  public void setIndex(String[] index) {
    this.index = index;
  }

  public String[] getField() {
    return field;
  }

  public void setField(String[] field) {
    this.field = field;
  }

  public ConceptGraphConfig getConceptGraph() {
    return conceptGraph;
  }

  public void setConceptGraph(ConceptGraphConfig conceptGraph) {
    this.conceptGraph = conceptGraph;
  }

  public VectorStoreConfig getVectorStore() {
    return vectorStore;
  }

  public void setVectorStore(VectorStoreConfig vectorStore) {
    this.vectorStore = vectorStore;
  }

  @Override
  public String toString() {
    return "TextAdapterConfig [id="
        + id
        + ", adapter="
        + adapter
        + ", connection="
        + connection
        + ", dateField="
        + dateField
        + "]";
  }
}
