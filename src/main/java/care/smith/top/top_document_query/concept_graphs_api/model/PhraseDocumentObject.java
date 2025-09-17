package care.smith.top.top_document_query.concept_graphs_api.model;

import java.util.List;

public class PhraseDocumentObject {
  private String id;
  private List<Integer[]> offsets;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Integer[]> getOffsets() {
    return offsets;
  }

  public void setOffsets(List<Integer[]> offsets) {
    this.offsets = offsets;
  }
}
