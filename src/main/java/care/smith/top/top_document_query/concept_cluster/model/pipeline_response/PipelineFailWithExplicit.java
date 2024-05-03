package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import care.smith.top.model.PipelineResponse;
import care.smith.top.model.PipelineResponseStatus;

public class PipelineFailWithExplicit extends PipelineFailEntity {
  private String error;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  @Override
  public PipelineResponse getSpecificResponse() {
    return new PipelineResponse()
        .pipelineId(this.getName())
        .response(this.getError())
        .status(PipelineResponseStatus.FAILED);
  }
}
