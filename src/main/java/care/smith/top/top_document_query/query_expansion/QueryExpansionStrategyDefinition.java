package care.smith.top.top_document_query.query_expansion;

public class QueryExpansionStrategyDefinition {
  private String id;
  private String title;
  private String description;
  private boolean expressionSupported;

  public QueryExpansionStrategyDefinition(
      String id, String title, String description, boolean expressionSupported) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.expressionSupported = expressionSupported;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public boolean isExpressionSupported() {
    return expressionSupported;
  }
}
