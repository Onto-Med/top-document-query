package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import care.smith.top.model.ConceptGraphPipelineStatusEnum;
import care.smith.top.model.ConceptGraphPipelineStepsEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Logger;

public class PipelineStatus {
  private static final Logger LOGGER = Logger.getLogger(PipelineStatus.class.getName());

  private ConceptGraphPipelineStepsEnum name;
  private ConceptGraphPipelineStatusEnum status;
  private int rank;

  public ConceptGraphPipelineStepsEnum getName() {
    return name;
  }

  public void setName(String name) {
    this.name = ConceptGraphPipelineStepsEnum.fromValue(name);
  }

  public ConceptGraphPipelineStatusEnum getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = ConceptGraphPipelineStatusEnum.fromValue(status);
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  //  @JsonProperty("data")
  //  private String dataStatus;
  //
  //  @JsonProperty("embedding")
  //  private String embeddingStatus;
  //
  //  @JsonProperty("clustering")
  //  private String clusteringStatus;
  //
  //  @JsonProperty("graph")
  //  private String graphStatus;
  //
  //  public String getDataStatus() {
  //    return dataStatus;
  //  }
  //
  //  public void setDataStatus(String dataStatus) {
  //    this.dataStatus = dataStatus;
  //  }
  //
  //  public String getEmbeddingStatus() {
  //    return embeddingStatus;
  //  }
  //
  //  public void setEmbeddingStatus(String embeddingStatus) {
  //    this.embeddingStatus = embeddingStatus;
  //  }
  //
  //  public String getClusteringStatus() {
  //    return clusteringStatus;
  //  }
  //
  //  public void setClusteringStatus(String clusteringStatus) {
  //    this.clusteringStatus = clusteringStatus;
  //  }
  //
  //  public String getGraphStatus() {
  //    return graphStatus;
  //  }
  //
  //  public void setGraphStatus(String graphStatus) {
  //    this.graphStatus = graphStatus;
  //  }

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
