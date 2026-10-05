package care.smith.top.top_document_query.query_expansion;

import java.util.Map;
import java.util.Optional;

/**
 * Central TOP-owned mapping from Concept Graphs semantic relation IDs to executable TOP query
 * expansion strategies.
 */
public final class QueryExpansionRelationStrategyResolver {
  private static final Map<String, QueryExpansionStrategy> STRATEGIES_BY_RELATION_ID =
      Map.ofEntries(
          Map.entry("equivalent_to", QueryExpansionStrategy.ALTERNATIVES),
          Map.entry("related_to", QueryExpansionStrategy.OPTIONAL_CONTEXT),
          Map.entry("may_indicate", QueryExpansionStrategy.REQUIRE_CONTEXT),
          Map.entry("treated_by", QueryExpansionStrategy.REQUIRE_CONTEXT),
          Map.entry("investigated_by", QueryExpansionStrategy.REQUIRE_CONTEXT),
          Map.entry("confirmed_by", QueryExpansionStrategy.REQUIRE_CONTEXT),
          Map.entry("broader_than", QueryExpansionStrategy.RECALL_EXPANSION),
          Map.entry("narrower_than", QueryExpansionStrategy.SPECIFICITY_FOCUS));

  private QueryExpansionRelationStrategyResolver() {}

  public static Optional<QueryExpansionStrategy> getStrategy(String relationId) {
    return Optional.ofNullable(STRATEGIES_BY_RELATION_ID.get(relationId));
  }
}
