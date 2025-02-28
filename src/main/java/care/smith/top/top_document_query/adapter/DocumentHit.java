package care.smith.top.top_document_query.adapter;

import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

public class DocumentHit {
  private DocumentEntity document;
  private String documentId;
  private Map<String, List<Pair<Integer, Integer>>> highlights;
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

  /**
   * @return A {@link Map} that contains a {@link List} of offsets for highlighted terms as {@link
   *     Pair} by fields.
   */
  public Map<String, List<Pair<Integer, Integer>>> getHighlights() {
    return highlights;
  }

  public DocumentHit setHighlights(Map<String, List<String>> highlights) {
    this.highlights =
        (highlights != null && !highlights.isEmpty())
            ? calculateHighlightOffsets(highlights)
            : null;
    return this;
  }

  /**
   * Highlights will be searched by looking for Terms between standard {@literal <em>TERM</em>} tags
   *
   * @param highlights
   */
  private Map<String, List<Pair<Integer, Integer>>> calculateHighlightOffsets(
      Map<String, List<String>> highlights) {
    return calculateHighlightOffsets(highlights, "em");
  }

  /**
   * Highlights will be searched by looking for Terms between {@literal <tag>TERM</tag>}
   *
   * @param highlights
   * @param tag
   */
  private Map<String, List<Pair<Integer, Integer>>> calculateHighlightOffsets(
      Map<String, List<String>> highlights, String tag) {
    return calculateHighlightOffsets(
        highlights, String.format("<%s>", tag), String.format("</%s>", tag));
  }

  /**
   * Highlights will be searched by looking for terms between {@literal <preTag>TERM<postTag>} Be
   * aware that this method uses Regex and some characters might have a special meaning.
   *
   * @param highlights
   * @param preTag
   * @param postTag
   */
  private Map<String, List<Pair<Integer, Integer>>> calculateHighlightOffsets(
      Map<String, List<String>> highlights, String preTag, String postTag) {
    // ToDo: offsets are sometimes not right?! e.g. Albers document. There seems to be something in
    // there that moves the offset
    Pattern pattern = Pattern.compile(String.format("%s(.*?)%s", preTag, postTag));
    Map<String, List<Pair<Integer, Integer>>> offsets = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
      int offsetCorrection = 0;
      Matcher matcher = pattern.matcher(entry.getValue().get(0));
      while (matcher.find()) {
        int begin = matcher.start() - offsetCorrection;
        int end = matcher.end() - (preTag + postTag).length() - offsetCorrection;
        offsetCorrection += (preTag + postTag).length();

        if (!offsets.containsKey(entry.getKey())) offsets.put(entry.getKey(), new ArrayList<>());
        offsets.get(entry.getKey()).add(Pair.of(begin, end));
      }
    }
    return offsets;
  }
}
