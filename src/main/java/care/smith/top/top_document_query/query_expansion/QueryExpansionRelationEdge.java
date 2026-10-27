package care.smith.top.top_document_query.query_expansion;

import java.util.Objects;

/** A resolved semantic relation edge whose endpoints are TOP entity IDs. */
public record QueryExpansionRelationEdge(
    String mappingId,
    String relationId,
    QueryExpansionStrategy strategy,
    String sourceEntityId,
    String targetEntityId) {

  public QueryExpansionRelationEdge {
    requireNonBlank(mappingId, "mappingId");
    requireNonBlank(relationId, "relationId");
    Objects.requireNonNull(strategy, "strategy must not be null");
    requireNonBlank(sourceEntityId, "sourceEntityId");
    requireNonBlank(targetEntityId, "targetEntityId");
  }

  private static void requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
  }
}
