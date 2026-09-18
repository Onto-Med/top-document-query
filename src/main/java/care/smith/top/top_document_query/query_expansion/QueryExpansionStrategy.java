package care.smith.top.top_document_query.query_expansion;

import java.util.Arrays;
import java.util.List;

public enum QueryExpansionStrategy {
  ALTERNATIVES(
      "alternatives",
      "Alternatives",
      "Treat source and target concepts as alternative labels or interchangeable expansion terms.",
      true),
  OPTIONAL_CONTEXT(
      "optional_context",
      "Optional context",
      "Use the target concept as optional context that may broaden the query.",
      true),
  REQUIRE_CONTEXT(
      "require_context",
      "Required context",
      "Require source and target concepts to appear as shared query context.",
      true),
  EXCLUDE_CONTEXT(
      "exclude_context",
      "Excluded context",
      "Exclude documents matching the target concept from the source concept context.",
      true),
  RECALL_EXPANSION(
      "recall_expansion",
      "Recall expansion",
      "Broaden the query to increase recall.",
      true),
  SPECIFICITY_FOCUS(
      "specificity_focus",
      "Specificity focus",
      "Narrow the query to increase specificity.",
      true),
  PROXIMITY_CONTEXT(
      "proximity_context",
      "Proximity context",
      "Require source and target concepts to appear near each other.",
      true),
  PHRASE_CONTEXT(
      "phrase_context",
      "Phrase context",
      "Generate or search combined source-target phrase context.",
      true),
  IGNORE(
      "ignore",
      "Ignore",
      "Do not compile this relation into a generated expression.",
      true);

  private final String id;
  private final String title;
  private final String description;
  private final boolean expressionSupported;

  QueryExpansionStrategy(
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

  public QueryExpansionStrategyDefinition toDefinition() {
    return new QueryExpansionStrategyDefinition(id, title, description, expressionSupported);
  }

  public static List<QueryExpansionStrategyDefinition> getDefinitions() {
    return Arrays.stream(values()).map(QueryExpansionStrategy::toDefinition).toList();
  }

  public static QueryExpansionStrategy fromId(String id) {
    return Arrays.stream(values())
        .filter(strategy -> strategy.id.equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown query expansion strategy: " + id));
  }
}
