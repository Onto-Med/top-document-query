package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Logger;

public class PipelineStatus {
  private static final Logger LOGGER = Logger.getLogger(PipelineStatus.class.getName());

  @JsonProperty("data")
  private String dataStatus;

  @JsonProperty("embedding")
  private String embeddingStatus;

  @JsonProperty("clustering")
  private String clusteringStatus;

  @JsonProperty("graph")
  private String graphStatus;

  public String getDataStatus() {
    return dataStatus;
  }

  public void setDataStatus(String dataStatus) {
    this.dataStatus = dataStatus;
  }

  public String getEmbeddingStatus() {
    return embeddingStatus;
  }

  public void setEmbeddingStatus(String embeddingStatus) {
    this.embeddingStatus = embeddingStatus;
  }

  public String getClusteringStatus() {
    return clusteringStatus;
  }

  public void setClusteringStatus(String clusteringStatus) {
    this.clusteringStatus = clusteringStatus;
  }

  public String getGraphStatus() {
    return graphStatus;
  }

  public void setGraphStatus(String graphStatus) {
    this.graphStatus = graphStatus;
  }

  public String toJsonString() {
    String status = "";
    try {
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      status = mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LOGGER.severe(e.getMessage());
    }
    return status;
  }
}
