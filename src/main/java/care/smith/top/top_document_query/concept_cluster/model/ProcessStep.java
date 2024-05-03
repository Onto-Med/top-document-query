package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.ConceptGraphPipelineStatusEnum;
import care.smith.top.model.ConceptGraphPipelineStepsEnum;

public class ProcessStep {
  private int rank;
  private ConceptGraphPipelineStepsEnum name;

  private ConceptGraphPipelineStatusEnum status;

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public ConceptGraphPipelineStepsEnum getName() {
    return name;
  }

  public void setName(ConceptGraphPipelineStepsEnum name) {
    this.name = name;
  }

  public ConceptGraphPipelineStatusEnum getStatus() {
    return status;
  }

  public void setStatus(ConceptGraphPipelineStatusEnum status) {
    this.status = status;
  }
}
