package care.smith.top.top_document_query.query_expansion;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.functions.Or;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Compiles resolved semantic relation graphs into deterministic query-expansion bundles. */
public final class QueryExpansionBundleCompiler {
  private static final Comparator<QueryExpansionRelationEdge> EDGE_ORDER =
      Comparator.comparing(QueryExpansionRelationEdge::mappingId)
          .thenComparing(QueryExpansionRelationEdge::relationId)
          .thenComparing(edge -> edge.strategy().getId())
          .thenComparing(QueryExpansionRelationEdge::sourceEntityId)
          .thenComparing(QueryExpansionRelationEdge::targetEntityId);

  private static final Comparator<GroupKey> GROUP_ORDER =
      Comparator.comparing(GroupKey::mappingId)
          .thenComparing(GroupKey::relationId)
          .thenComparing(group -> group.strategy().getId());

  private QueryExpansionBundleCompiler() {}

  /**
   * Groups edges by mapping, relation, strategy, and connected component, then combines the
   * per-edge expressions in each component with {@code OR}.
   */
  public static List<QueryExpansionBundle> compile(
      Collection<QueryExpansionRelationEdge> relationEdges) {
    Objects.requireNonNull(relationEdges, "relationEdges must not be null");

    List<QueryExpansionRelationEdge> orderedEdges =
        relationEdges.stream()
            .map(edge -> Objects.requireNonNull(edge, "relationEdges must not contain null"))
            .sorted(EDGE_ORDER)
            .distinct()
            .toList();

    Map<GroupKey, List<QueryExpansionRelationEdge>> groupedEdges = new TreeMap<>(GROUP_ORDER);
    for (QueryExpansionRelationEdge edge : orderedEdges) {
      GroupKey key = new GroupKey(edge.mappingId(), edge.relationId(), edge.strategy());
      groupedEdges.computeIfAbsent(key, ignored -> new ArrayList<>()).add(edge);
    }

    List<QueryExpansionBundle> bundles = new ArrayList<>();
    groupedEdges.forEach(
        (group, edges) -> {
          for (Set<String> component : connectedComponents(edges)) {
            List<QueryExpansionRelationEdge> componentEdges =
                edges.stream()
                    .filter(
                        edge ->
                            component.contains(edge.sourceEntityId())
                                && component.contains(edge.targetEntityId()))
                    .toList();
            compileComponent(group, componentEdges).ifPresent(bundles::add);
          }
        });
    return List.copyOf(bundles);
  }

  private static Optional<QueryExpansionBundle> compileComponent(
      GroupKey group, List<QueryExpansionRelationEdge> edges) {
    List<Expression> expressions =
        edges.stream()
            .map(
                edge ->
                    QueryExpansionExpressionCompiler.compileRelation(
                        edge.strategy(), edge.sourceEntityId(), edge.targetEntityId()))
            .flatMap(Optional::stream)
            .toList();
    if (expressions.isEmpty()) return Optional.empty();

    Expression expression = expressions.size() == 1 ? expressions.get(0) : Or.of(expressions);
    Set<String> sourceEntityIds = new TreeSet<>();
    Set<String> targetEntityIds = new TreeSet<>();
    edges.forEach(
        edge -> {
          sourceEntityIds.add(edge.sourceEntityId());
          targetEntityIds.add(edge.targetEntityId());
        });

    return Optional.of(
        new QueryExpansionBundle(
            group.mappingId(),
            group.relationId(),
            group.strategy(),
            new LinkedHashSet<>(sourceEntityIds),
            new LinkedHashSet<>(targetEntityIds),
            edges,
            expression));
  }

  private static List<Set<String>> connectedComponents(
      List<QueryExpansionRelationEdge> edges) {
    Map<String, Set<String>> adjacency = new TreeMap<>();
    for (QueryExpansionRelationEdge edge : edges) {
      adjacency
          .computeIfAbsent(edge.sourceEntityId(), ignored -> new TreeSet<>())
          .add(edge.targetEntityId());
      adjacency
          .computeIfAbsent(edge.targetEntityId(), ignored -> new TreeSet<>())
          .add(edge.sourceEntityId());
    }

    Set<String> visited = new TreeSet<>();
    List<Set<String>> components = new ArrayList<>();
    for (String firstEntityId : adjacency.keySet()) {
      if (!visited.add(firstEntityId)) continue;

      Set<String> component = new LinkedHashSet<>();
      Deque<String> pending = new ArrayDeque<>();
      pending.add(firstEntityId);
      while (!pending.isEmpty()) {
        String entityId = pending.removeFirst();
        component.add(entityId);
        for (String neighbor : adjacency.get(entityId)) {
          if (visited.add(neighbor)) pending.addLast(neighbor);
        }
      }
      components.add(component);
    }
    return components;
  }

  private record GroupKey(
      String mappingId, String relationId, QueryExpansionStrategy strategy) {}
}
