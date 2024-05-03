package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.PipelineResponse;
import care.smith.top.model.PipelineResponseStatus;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineResponseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import java.util.logging.Logger;

public class ConceptGraphStatisticsEntity implements PipelineResponseEntity {
  private static final Logger LOGGER =
      Logger.getLogger(ConceptGraphStatisticsEntity.class.getName());
  private String name;
  private int numberOfGraphs;
  private GraphStatsEntity[] conceptGraphs;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getNumberOfGraphs() {
    return numberOfGraphs;
  }

  public void setNumberOfGraphs(int numberOfGraphs) {
    this.numberOfGraphs = numberOfGraphs;
  }

  public GraphStatsEntity[] getConceptGraphs() {
    return conceptGraphs;
  }

  public void setConceptGraphs(GraphStatsEntity[] conceptGraphs) {
    this.conceptGraphs = conceptGraphs;
  }

  @Override
  public PipelineResponse getSpecificResponse() {
    if (numberOfGraphs == 0 || conceptGraphs == null || conceptGraphs.length == 0) {
      return new PipelineResponse()
          .pipelineId(this.getName())
          .response("There seem to be no concept graphs available.")
          .status(PipelineResponseStatus.FAILED);
    }
    String status = "";
    try {
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      status =
          mapper.writeValueAsString(
              Map.of(
                  "conceptGraphs", conceptGraphs,
                  "numberOfGraphs", numberOfGraphs));
    } catch (JsonProcessingException e) {
      LOGGER.severe(e.getMessage());
    }
    return new PipelineResponse()
        .pipelineId(this.getName())
        .response(status)
        .status(PipelineResponseStatus.SUCCESSFUL);
  }
}
