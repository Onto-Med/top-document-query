package care.smith.top.top_document_query.query_expansion;

import care.smith.top.model.Expression;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A compiled semantic bundle and the resolved relation edges that contributed to it. */
public record QueryExpansionBundle(
    String mappingId,
    String relationId,
    QueryExpansionStrategy strategy,
    Set<String> sourceEntityIds,
    Set<String> targetEntityIds,
    List<QueryExpansionRelationEdge> edges,
    Expression expression) {

  public QueryExpansionBundle {
    Objects.requireNonNull(mappingId, "mappingId must not be null");
    Objects.requireNonNull(relationId, "relationId must not be null");
    Objects.requireNonNull(strategy, "strategy must not be null");
    sourceEntityIds = immutableOrderedSet(sourceEntityIds, "sourceEntityIds");
    targetEntityIds = immutableOrderedSet(targetEntityIds, "targetEntityIds");
    edges = List.copyOf(Objects.requireNonNull(edges, "edges must not be null"));
    Objects.requireNonNull(expression, "expression must not be null");
  }

  private static <T> Set<T> immutableOrderedSet(Set<T> values, String name) {
    Objects.requireNonNull(values, name + " must not be null");
    return Collections.unmodifiableSet(new LinkedHashSet<>(values));
  }
}
