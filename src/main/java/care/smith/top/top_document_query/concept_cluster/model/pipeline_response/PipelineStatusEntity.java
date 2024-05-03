package care.smith.top.top_document_query.concept_cluster.model.pipeline_response;

import care.smith.top.model.PipelineResponse;
import care.smith.top.model.PipelineResponseStatus;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PipelineStatusEntity implements PipelineResponseEntity {
  private String name;
  private PipelineStatus[] status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PipelineStatus[] getStatus() {
    return status;
  }

  public void setStatus(PipelineStatus[] status) {
    this.status = status;
  }

  @Override
  public PipelineResponse getSpecificResponse() {
    return new PipelineResponse()
        .pipelineId(this.getName())
        .response(
            Arrays.stream(this.getStatus())
                .map(PipelineStatus::toJsonString)
                .collect(Collectors.joining("\n")))
        .status(PipelineResponseStatus.SUCCESSFUL);
  }
}
