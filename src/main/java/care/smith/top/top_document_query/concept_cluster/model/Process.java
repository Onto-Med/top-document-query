package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.ConceptGraphPipeline;
import care.smith.top.model.ConceptGraphPipelineFinishedSteps;
import care.smith.top.model.ConceptGraphPipelineStatus;
import care.smith.top.model.PipelineResponseStatus;

public class Process {
  private String name;
  private ProcessStep[] status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProcessStep[] getStatus() {
    return status;
  }

  public void setStatus(ProcessStep[] status) {
    this.status = status;
  }

  public ConceptGraphPipeline toApiModel() {
    ConceptGraphPipeline process = new ConceptGraphPipeline();
    process.setPipelineId(getName());
    if (status != null) {
      for (ProcessStep processStep : getStatus()) {
        ConceptGraphPipelineStatus step = new ConceptGraphPipelineStatus();
        step.setName(processStep.getName());
        step.setRank(processStep.getRank());
        step.setStatus(processStep.getStatus());
        process.addStepsItem(step);
      }
    }
    return process;
  }

  public ConceptGraphPipeline toApiModel(PipelineResponseStatus status) {
    return toApiModel().status(status);
  }
}
