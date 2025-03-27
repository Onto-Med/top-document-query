package care.smith.top.top_document_query.elasticsearch;

import care.smith.top.model.Document;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentEntity {
  // Attributes extracted from Elasticsearch source
  private String id;

  private String name;

  private String text;

  private String label; // label is not used at the moment but potentially useful

  // Other attributes
  private Map<String, List<String>> highlights;

  private final Integer wordEllipsis = 30;

  public Document toApiModel() {
    return new Document()
        .id(getId())
        .name(getName())
        .text(getText())
        .highlightedText(highlights == null ? null :
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public Document toApiModel(String externalId) {
    return new Document()
        .id(externalId)
        .name(getName())
        .text(getText())
        .highlightedText(highlights == null ? null :
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public Document toApiModel(String externalId, Integer ellipsis) {
    return new Document()
        .id(externalId)
        .name(getName())
        .text(getText())
        .highlightedText(highlights == null ? null :
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public Document toApiModel(Integer ellipsis) {
    return new Document()
        .id(id)
        .name(name)
        .text(getText(ellipsis))
        .highlightedText(highlights == null ? null :
            this.getHighlights().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.joining()));
  }

  public Document toSimplifiedApiModel() {
    return new Document().id(id).name(name).text(getText(wordEllipsis));
  }

  public Document toSimplifiedApiModel(String eid) {
    return new Document().id(eid).name(name).text(getText(wordEllipsis));
  }

  public static Document nullDocument() {
    return new Document().id("null").name("null").text("null").highlightedText("null");
  }

  public String getId() {
    return id;
  }

  public DocumentEntity setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public DocumentEntity setName(String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public String getText(Integer ellipsis) {
    return (ellipsis != null && ellipsis > 0)
        ? Arrays.stream(text.split("\\s+")).limit(ellipsis).collect(Collectors.joining(" "))
        : text;
  }

  public DocumentEntity setText(String text) {
    this.text = text;
    return this;
  }

  public Map<String, List<String>> getHighlights() {
    return highlights;
  }

  public DocumentEntity setHighlights(Map<String, List<String>> highlights) {
    this.highlights = highlights;
    return this;
  }
}
