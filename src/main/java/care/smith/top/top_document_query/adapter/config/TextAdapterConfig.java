package care.smith.top.top_document_query.adapter.config;

import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchAdapter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TextAdapterConfig {
  private static final Logger LOGGER = Logger.getLogger(TextAdapterConfig.class.getName());

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

  public static TextAdapterConfig getInstance(String yamlFilePath) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    TextAdapterConfig config = null;
    try {
      config = mapper.readValue(new File(yamlFilePath), TextAdapterConfig.class);
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    return config;
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
