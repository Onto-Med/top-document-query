package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.ConceptGraphPipeline;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessOverviewEntity {
  private Process[] processes;

  public Process[] getProcesses() {
    return processes;
  }

  public void setProcesses(Process[] processes) {
    this.processes = processes;
  }

  public List<ConceptGraphPipeline> toApiModel() {
    return Arrays.stream(getProcesses()).map(Process::toApiModel).collect(Collectors.toList());
  }
}
