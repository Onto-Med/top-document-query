package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.ConceptGraphProcess;
import care.smith.top.model.ConceptGraphProcessFinishedSteps;

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

  public ConceptGraphProcess toApiModel() {
    ConceptGraphProcess process = new ConceptGraphProcess();
    process.setName(getName());
    for (ProcessStep processStep : getFinishedSteps()) {
      ConceptGraphProcessFinishedSteps finishedSteps = new ConceptGraphProcessFinishedSteps();
      finishedSteps.setName(processStep.getName());
      finishedSteps.setRank(processStep.getRank());
      process.addFinishedStepsItem(finishedSteps);
    }
    return process;
  }
}
