package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.functions.XProd;
import care.smith.top.top_document_query.query_expansion.QueryExpansionExpressionNormalizer;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class QueryExpansionExpressionNormalizerTest {

  @Test
  public void flattensAndDeduplicatesAssociativeExpressions() {
    Expression expression =
        Or.of(Exp.ofEntity("b"), Or.of(Exp.ofEntity("a"), Exp.ofEntity("b")));

    Expression normalized = QueryExpansionExpressionNormalizer.normalize(expression);

    assertEquals(Or.ID, normalized.getFunctionId());
    assertEquals(
        List.of("a", "b"),
        normalized.getArguments().stream().map(Expression::getEntityId).toList());
  }

  @Test
  public void factorsConjunctsCommonToEveryDisjunctionBranch() {
    Expression expression =
        Or.of(
            And.of(Exp.ofEntity("source"), Exp.ofEntity("b")),
            And.of(Exp.ofEntity("source"), Exp.ofEntity("a")));

    Expression normalized = QueryExpansionExpressionNormalizer.normalize(expression);

    assertEquals(And.ID, normalized.getFunctionId());
    assertEquals("source", normalized.getArguments().get(0).getEntityId());
    Expression alternatives = normalized.getArguments().get(1);
    assertEquals(Or.ID, alternatives.getFunctionId());
    assertEquals(
        List.of("a", "b"),
        alternatives.getArguments().stream().map(Expression::getEntityId).toList());
    assertEquivalent(expression, normalized, List.of("source", "a", "b"));
  }

  @Test
  public void normalizationIsDeterministicAndIdempotent() {
    Expression first =
        Or.of(
            And.of(Exp.ofEntity("source"), Exp.ofEntity("b")),
            And.of(Exp.ofEntity("source"), Exp.ofEntity("a")));
    Expression second =
        Or.of(
            And.of(Exp.ofEntity("a"), Exp.ofEntity("source")),
            And.of(Exp.ofEntity("b"), Exp.ofEntity("source")));

    Expression normalized = QueryExpansionExpressionNormalizer.normalize(first);

    assertEquals(normalized, QueryExpansionExpressionNormalizer.normalize(second));
    assertEquals(normalized, QueryExpansionExpressionNormalizer.normalize(normalized));
  }

  @Test
  public void onlyAppliesLogicallyEquivalentExclusionFactoring() {
    Expression expression =
        Or.of(
            And.of(Exp.ofEntity("source"), Not.of(Exp.ofEntity("a"))),
            And.of(Exp.ofEntity("source"), Not.of(Exp.ofEntity("b"))));

    Expression normalized = QueryExpansionExpressionNormalizer.normalize(expression);

    assertEquals(And.ID, normalized.getFunctionId());
    assertEquals(Or.ID, normalized.getArguments().get(1).getFunctionId());
    assertEquivalent(expression, normalized, List.of("source", "a", "b"));
  }

  @Test
  public void preservesTruthTablesAcrossSupportedRewrites() {
    Expression a = Exp.ofEntity("a");
    Expression b = Exp.ofEntity("b");
    Expression c = Exp.ofEntity("c");
    Expression source = Exp.ofEntity("source");
    List<Expression> expressions =
        List.of(
            And.of(a, And.of(b, a), c),
            Or.of(a, Or.of(b, a), c),
            Or.of(And.of(source, a, c), And.of(source, b, c)),
            Or.of(And.of(source, Not.of(a)), And.of(source, Not.of(b))));

    for (Expression expression : expressions) {
      Expression normalized = QueryExpansionExpressionNormalizer.normalize(expression);
      assertEquivalent(expression, normalized, List.of("source", "a", "b", "c"));
      assertEquals(normalized, QueryExpansionExpressionNormalizer.normalize(normalized));
    }
  }

  @Test
  public void preservesArgumentPairingForOpaqueFunctions() {
    Expression phrase = XProd.of(Exp.ofEntity("source"), Exp.ofEntity("target"));

    Expression normalized = QueryExpansionExpressionNormalizer.normalize(phrase);

    assertEquals(XProd.ID, normalized.getFunctionId());
    assertEquals(
        List.of("source", "target"),
        normalized.getArguments().stream().map(Expression::getEntityId).toList());
  }

  private void assertEquivalent(
      Expression original, Expression normalized, List<String> variables) {
    for (int assignment = 0; assignment < (1 << variables.size()); assignment++) {
      Map<String, Boolean> values = new java.util.HashMap<>();
      for (int index = 0; index < variables.size(); index++) {
        values.put(variables.get(index), (assignment & (1 << index)) != 0);
      }
      assertEquals(
          evaluate(original, values),
          evaluate(normalized, values),
          "Expressions differ for assignment " + values);
    }
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
}
