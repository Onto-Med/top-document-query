package care.smith.top.top_document_query.concept_graphs_api.model;

public class PhraseNodeObject {
  private String id;
  private String label;
  private PhraseDocumentObject[] documents;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public PhraseDocumentObject[] getDocuments() {
    return documents;
  }

  public void setDocuments(PhraseDocumentObject[] documents) {
    this.documents = documents;
  }
}
