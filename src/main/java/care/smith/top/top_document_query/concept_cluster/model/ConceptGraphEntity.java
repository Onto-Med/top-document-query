package care.smith.top.top_document_query.concept_cluster.model;

import care.smith.top.model.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConceptGraphEntity {
  private AdjacencyObject[] adjacency;
  private PhraseNodeObject[] nodes;

  public AdjacencyObject[] getAdjacency() {
    return adjacency;
  }

  public void setAdjacency(AdjacencyObject[] adjacency) {
    this.adjacency = adjacency;
  }

  public PhraseNodeObject[] getNodes() {
    return nodes;
  }

  public void setNodes(PhraseNodeObject[] nodes) {
    this.nodes = nodes;
  }

  public ConceptGraph toApiModel() {
    ConceptGraph conceptGraph = new ConceptGraph();

    for (AdjacencyObject adj : getAdjacency()) {
      ConceptGraphAdjacency conceptGraphAdjacency = new ConceptGraphAdjacency();
      conceptGraphAdjacency.setId(adj.getId());
      for (PhraseNodeNeighbors neighbors : adj.getNeighbors()) {
        conceptGraphAdjacency.addNeighborsItem(
            new ConceptGraphNeighbors()
                .id(neighbors.getId())
                .significance(neighbors.getSignificance())
                .weight(neighbors.getWeight()));
      }
      conceptGraph.addAdjacencyItem(conceptGraphAdjacency);
    }

    for (PhraseNodeObject node : getNodes()) {
      conceptGraph.addNodesItem(
          new ConceptGraphNodes()
              .id(node.getId())
              .label(node.getLabel())
              .documents(
                  Arrays.stream(node.getDocuments())
                      .map(
                          phraseDocumentObject -> {
                            return new NodeDocuments()
                                .id(phraseDocumentObject.getId())
                                .offsets(
                                    phraseDocumentObject.getOffsets().stream()
                                        .map(integers -> Arrays.stream(integers).toList())
                                        .toList());
                          })
                      .collect(Collectors.toList())));
    }

    return conceptGraph;
  }
}
