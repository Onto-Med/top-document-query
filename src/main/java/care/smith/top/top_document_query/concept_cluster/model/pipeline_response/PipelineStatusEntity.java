package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import care.smith.top.model.PipelineResponse;
import care.smith.top.model.PipelineResponseStatus;

public class PipelineStatusEntity implements PipelineResponseEntity {
  private String name;
  private PipelineStatus status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PipelineStatus getStatus() {
    return status;
  }

  public void setStatus(PipelineStatus status) {
    this.status = status;
  }

  @Override
  public PipelineResponse getSpecificResponse() {
    return new PipelineResponse()
        .name(this.getName())
        .response(this.getStatus().toJsonString())
        .status(PipelineResponseStatus.SUCCESSFUL);
  }
}
