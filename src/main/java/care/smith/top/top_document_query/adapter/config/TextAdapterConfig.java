package care.smith.top.top_document_query.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

public class TextAdapterConfig {

  private String id;
  private String adapter;
  private Connection connection;
  private String dateField;
  private String[] index;
  private String[] field;
  private ConceptGraphConfig conceptGraph;
  private GraphDBConfig graphDB;

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

  public GraphDBConfig getGraphDB() {
    return graphDB;
  }

  public void setGraphDB(GraphDBConfig graphDB) {
    this.graphDB = graphDB;
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
