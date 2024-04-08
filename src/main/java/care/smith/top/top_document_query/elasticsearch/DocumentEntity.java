package care.smith.top.top_document_query.elasticsearch;

import care.smith.top.model.Document;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentEntity {
  private String id;

  private Integer wordEllipsis = 25;

  private String name;

  private String text;

  private String label;

  private Map<String, List<String>> highlights;

  public Document toApiModel() {
    return new Document()
        .id(id)
        .name(name)
        .text(
            Arrays.stream(text.split("\\s+")).limit(wordEllipsis).collect(Collectors.joining(" ")))
        .highlightedText(
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public Document toApiModel(String eid) {
    return new Document()
        .id(eid)
        .name(name)
        .text(
            Arrays.stream(text.split("\\s+")).limit(wordEllipsis).collect(Collectors.joining(" ")))
        .highlightedText(
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public static Document nullDocument() {
    return new Document().id("null").name("null").text("null").highlightedText("null");
  }

  public String getId() {
    return id;
  }

  public Integer getWordEllipsis() {
    return wordEllipsis;
  }

  public void setWordEllipsis(Integer wordEllipsis) {
    this.wordEllipsis = wordEllipsis;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, List<String>> getHighlights() {
    if (this.highlights == null) {
      return Map.of("documentText", List.of(getText()));
    }
    return highlights;
  }

  public void setHighlights(Map<String, List<String>> highlights) {
    this.highlights = highlights;
  }
}
