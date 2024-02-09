package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import care.smith.top.model.PipelineResponse;
import care.smith.top.model.PipelineResponseStatus;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineFailEntity;

public class PipelineFailWithExplicit extends PipelineFailEntity {
  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public PipelineResponse getSpecificResponse() {
    return new PipelineResponse()
        .name(this.getName())
        .response(this.getStatus())
        .status(PipelineResponseStatus.FAILED);
  }
}
