package care.smith.top.top_document_query.concept_cluster.model;

public class AdjacencyObject {
  private String id;
  private PhraseNodeNeighbors[] neighbors;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PhraseNodeNeighbors[] getNeighbors() {
    return neighbors;
  }

  public void setNeighbors(PhraseNodeNeighbors[] neighbors) {
    this.neighbors = neighbors;
  }
}
