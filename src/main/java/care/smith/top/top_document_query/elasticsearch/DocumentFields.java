package care.smith.top.top_document_query.elasticsearch;

public enum DocumentFields {
  TITLE("name"),
  TEXT("text"),
  ID("id"),
  LABEL("label");

  private final String value;

  DocumentFields(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
