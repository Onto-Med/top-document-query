package care.smith.top.top_document_query.adapter;

import care.smith.top.top_document_query.elasticsearch.DocumentEntity;

public class DocumentHit {
  private DocumentEntity document;
  private String documentId;
  private double score;

  public DocumentHit(String documentId, DocumentEntity document) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(0.0);
  }

  public DocumentHit(String documentId, DocumentEntity document, Double score) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(score);
  }

  public DocumentEntity getDocument() {
    return document;
  }

  public DocumentHit setDocument(DocumentEntity document) {
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
