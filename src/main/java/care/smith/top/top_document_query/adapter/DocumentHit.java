package care.smith.top.top_document_query.adapter;

import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import java.util.List;
import java.util.Map;

public class DocumentHit {
  private DocumentEntity document;
  private String documentId;
  private Map<String, List<String>> highlights;
  private double score;

  public DocumentHit(String documentId, DocumentEntity document) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(0.0);
    this.setHighlights(null);
  }

  public DocumentHit(
      String documentId,
      DocumentEntity document,
      Map<String, List<String>> highlights,
      Double score) {
    this.setDocumentId(documentId);
    this.setDocument(document);
    this.setScore(score);
    this.setHighlights(highlights);
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

  public Map<String, List<String>> getHighlights() {
    return highlights;
  }

  public DocumentHit setHighlights(Map<String, List<String>> highlights) {
    this.highlights = (highlights != null && !highlights.isEmpty()) ? highlights : null;
    return this;
  }
}
