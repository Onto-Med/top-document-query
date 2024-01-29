package care.smith.top.top_document_query.adapter;

public class DocumentHit {
  private AbstractDocument document;
  private String documentId;
  private double score;

  public DocumentHit(String documentId, AbstractDocument document) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(0.0);
  }

  public DocumentHit(String documentId, AbstractDocument document, Double score) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(score);
  }

  public AbstractDocument getDocument() {
    return document;
  }

  public DocumentHit setDocument(AbstractDocument document) {
    this.document = document;
    return this;
  }

  public String getDocumentId() {
    return documentId;
  }

  public DocumentHit setDocumentId(String documentId) {
    this.documentId = documentId;
    return this;
  }

  public double getScore() {
    return score;
  }

  public DocumentHit setScore(Double score) {
    this.score = score != null ? score : 0.0;
    return this;
  }
}
