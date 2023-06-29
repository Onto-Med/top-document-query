package care.smith.top.top_phenotypic_query.song.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TextAdapterConfig {

  private String id;
  private String adapter;
  private Map<String, String> connection;
  private String dateField;
  private String[] index;
  private String[] field;

  public static TextAdapterConfig getInstance(String yamlFilePath) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    TextAdapterConfig config = null;
    try {
      config = mapper.readValue(new File(yamlFilePath), TextAdapterConfig.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return config;
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

  public Map<String, String> getConnection() {
    return connection;
  }

  public String getConnectionAttribute(String name) {
    return connection.get(name);
  }

  public void setConnection(Map<String, String> connection) {
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
