package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.query_expansion.QueryExpansionBundle;
import care.smith.top.top_document_query.query_expansion.QueryExpansionBundleCompiler;
import care.smith.top.top_document_query.query_expansion.QueryExpansionRelationEdge;
import care.smith.top.top_document_query.query_expansion.QueryExpansionStrategy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class QueryExpansionBundleCompilerTest {

  @Test
  public void groupsConnectedEdgesIntoOneBundle() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                edge("mapping", "treated_by", "infarction", "clopidogrel"),
                edge("mapping", "treated_by", "infarction", "aspirin")));

    assertEquals(1, bundles.size());
    QueryExpansionBundle bundle = bundles.get(0);
    assertEquals(Set.of("infarction"), bundle.sourceEntityIds());
    assertEquals(Set.of("aspirin", "clopidogrel"), bundle.targetEntityIds());
    assertEquals(2, bundle.edges().size());
    assertEquals(And.ID, bundle.expression().getFunctionId());
    assertEquals("infarction", bundle.expression().getArguments().get(0).getEntityId());
    Expression alternatives = bundle.expression().getArguments().get(1);
    assertEquals(Or.ID, alternatives.getFunctionId());
    assertEquals(
        List.of("aspirin", "clopidogrel"),
        alternatives.getArguments().stream().map(Expression::getEntityId).toList());
  }

  @Test
  public void separatesDisconnectedComponents() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                edge("mapping", "treated_by", "pneumonia", "antibiotics"),
                edge("mapping", "treated_by", "infarction", "aspirin"),
                edge("mapping", "treated_by", "infarction", "clopidogrel")));

    assertEquals(2, bundles.size());
    assertEquals(
        Set.of(Set.of("infarction"), Set.of("pneumonia")),
        bundles.stream().map(QueryExpansionBundle::sourceEntityIds).collect(Collectors.toSet()));
  }

  @Test
  public void keepsMappingsAndRelationsSeparate() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                edge("mapping-a", "treated_by", "source", "target"),
                edge("mapping-b", "treated_by", "source", "target"),
                new QueryExpansionRelationEdge(
                    "mapping-a",
                    "confirmed_by",
                    QueryExpansionStrategy.REQUIRE_CONTEXT,
                    "source",
                    "target")));

    assertEquals(3, bundles.size());
    assertEquals(
        List.of("mapping-a:confirmed_by", "mapping-a:treated_by", "mapping-b:treated_by"),
        bundles.stream()
            .map(bundle -> bundle.mappingId() + ":" + bundle.relationId())
            .toList());
  }

  @Test
  public void removesDuplicateEdgesAndOmitsIgnoredComponents() {
    QueryExpansionRelationEdge included =
        edge("mapping", "treated_by", "infarction", "aspirin");
    QueryExpansionRelationEdge ignored =
        new QueryExpansionRelationEdge(
            "ignored-mapping",
            "related_to",
            QueryExpansionStrategy.IGNORE,
            "infarction",
            "context");

    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(List.of(included, included, ignored));

    assertEquals(1, bundles.size());
    assertEquals(1, bundles.get(0).edges().size());
    assertTrue(QueryExpansionBundleCompiler.compile(List.of()).isEmpty());
    Expression expression = bundles.get(0).expression();
    assertEquals(And.ID, expression.getFunctionId());
  }

  @Test
  public void constructsExclusionsPerSourceBeforeNormalization() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                new QueryExpansionRelationEdge(
                    "mapping",
                    "contraindicated_with",
                    QueryExpansionStrategy.EXCLUDE_CONTEXT,
                    "source",
                    "a"),
                new QueryExpansionRelationEdge(
                    "mapping",
                    "contraindicated_with",
                    QueryExpansionStrategy.EXCLUDE_CONTEXT,
                    "source",
                    "b")));

    assertEquals(1, bundles.size());
    Expression expression = bundles.get(0).expression();
    assertEquals(And.ID, expression.getFunctionId());
    assertEquals("source", expression.getArguments().get(0).getEntityId());
    Expression exclusion = expression.getArguments().get(1);
    assertEquals(Not.ID, exclusion.getFunctionId());
    assertEquals(Or.ID, exclusion.getArguments().get(0).getFunctionId());
    assertTrue(evaluate(expression, Map.of("source", true, "a", false, "b", false)));
    assertFalse(evaluate(expression, Map.of("source", true, "a", true, "b", false)));
    assertFalse(evaluate(expression, Map.of("source", true, "a", false, "b", true)));
  }

  @Test
  public void keepsAlternativeSourcesForExclusions() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                new QueryExpansionRelationEdge(
                    "mapping",
                    "contraindicated_with",
                    QueryExpansionStrategy.EXCLUDE_CONTEXT,
                    "source-a",
                    "target"),
                new QueryExpansionRelationEdge(
                    "mapping",
                    "contraindicated_with",
                    QueryExpansionStrategy.EXCLUDE_CONTEXT,
                    "source-b",
                    "target")));

    assertEquals(1, bundles.size());
    Expression expression = bundles.get(0).expression();
    assertTrue(
        evaluate(
            expression,
            Map.of("source-a", true, "source-b", false, "target", false)));
    assertTrue(
        evaluate(
            expression,
            Map.of("source-a", false, "source-b", true, "target", false)));
    assertFalse(
        evaluate(
            expression,
            Map.of("source-a", true, "source-b", false, "target", true)));
    assertFalse(
        evaluate(
            expression,
            Map.of("source-a", false, "source-b", false, "target", false)));
  }

  @Test
  public void groupsMultipleSourcesConnectedToOneTarget() {
    List<QueryExpansionBundle> bundles =
        QueryExpansionBundleCompiler.compile(
            List.of(
                edge("mapping", "treated_by", "condition-a", "therapy"),
                edge("mapping", "treated_by", "condition-b", "therapy")));

    assertEquals(1, bundles.size());
    assertEquals(Set.of("condition-a", "condition-b"), bundles.get(0).sourceEntityIds());
    assertEquals(Set.of("therapy"), bundles.get(0).targetEntityIds());
  }

  private boolean evaluate(Expression expression, Map<String, Boolean> values) {
    if (expression.getEntityId() != null) return values.get(expression.getEntityId());
    List<Expression> arguments = expression.getArguments();
    return switch (expression.getFunctionId()) {
      case And.ID -> arguments.stream().allMatch(argument -> evaluate(argument, values));
      case Or.ID -> arguments.stream().anyMatch(argument -> evaluate(argument, values));
      case Not.ID -> !evaluate(arguments.get(0), values);
      default -> throw new IllegalArgumentException(
          "Unsupported test expression: " + expression.getFunctionId());
    };
  }

  private QueryExpansionRelationEdge edge(
      String mappingId, String relationId, String sourceEntityId, String targetEntityId) {
    return new QueryExpansionRelationEdge(
        mappingId,
        relationId,
        QueryExpansionStrategy.REQUIRE_CONTEXT,
        sourceEntityId,
        targetEntityId);
  }
}
