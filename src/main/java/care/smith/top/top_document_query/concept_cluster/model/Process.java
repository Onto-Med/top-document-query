package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.ConceptGraphPipeline;
import care.smith.top.model.ConceptGraphPipelineFinishedSteps;

public class Process {
  private String name;
  private ProcessStep[] finishedSteps;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProcessStep[] getFinishedSteps() {
    return finishedSteps;
  }

  public void setFinishedSteps(ProcessStep[] finishedSteps) {
    this.finishedSteps = finishedSteps;
  }

  public ConceptGraphPipeline toApiModel() {
    ConceptGraphPipeline process = new ConceptGraphPipeline();
    process.setPipelineId(getName());
    if (finishedSteps != null) {
      for (ProcessStep processStep : getFinishedSteps()) {
        ConceptGraphPipelineFinishedSteps finishedSteps = new ConceptGraphPipelineFinishedSteps();
        finishedSteps.setName(processStep.getName());
        finishedSteps.setRank(processStep.getRank());
        process.addFinishedStepsItem(finishedSteps);
      }
    }
    return process;
  }
}
