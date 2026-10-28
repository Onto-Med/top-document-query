package care.smith.top.top_document_query.query_expansion;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Or;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Applies conservative, semantics-preserving normalization to TOP Boolean expressions. */
public final class QueryExpansionExpressionNormalizer {
  private static final Comparator<Expression> EXPRESSION_ORDER =
      Comparator.comparing(QueryExpansionExpressionNormalizer::sortKey)
          .thenComparing(QueryExpansionExpressionNormalizer::structuralKey);

  private QueryExpansionExpressionNormalizer() {}

  public static Expression normalize(Expression expression) {
    Objects.requireNonNull(expression, "expression must not be null");
    return normalizeRecursively(expression);
  }

  private static Expression normalizeRecursively(Expression expression) {
    List<Expression> arguments = expression.getArguments();
    if (arguments == null || arguments.isEmpty()) return expression;

    List<Expression> normalizedArguments =
        arguments.stream().map(QueryExpansionExpressionNormalizer::normalizeRecursively).toList();
    String functionId = expression.getFunctionId();
    if (!And.ID.equals(functionId) && !Or.ID.equals(functionId)) {
      return copyWithArguments(expression, normalizedArguments);
    }

    List<Expression> operands = flatten(functionId, normalizedArguments);
    operands = distinctAndOrdered(operands);
    if (operands.size() == 1) return operands.get(0);

    Expression normalized = copyWithArguments(expression, operands);
    if (Or.ID.equals(functionId)) {
      Expression factored = factorCommonConjuncts(operands);
      if (factored != null) return normalizeRecursively(factored);
    }
    return normalized;
  }

  private static List<Expression> flatten(String functionId, List<Expression> arguments) {
    List<Expression> flattened = new ArrayList<>();
    for (Expression argument : arguments) {
      if (functionId.equals(argument.getFunctionId()) && argument.getArguments() != null) {
        flattened.addAll(argument.getArguments());
      } else {
        flattened.add(argument);
      }
    }
    return flattened;
  }

  private static List<Expression> distinctAndOrdered(List<Expression> expressions) {
    Map<String, Expression> expressionsByKey = new LinkedHashMap<>();
    expressions.forEach(
        expression -> expressionsByKey.putIfAbsent(structuralKey(expression), expression));
    return expressionsByKey.values().stream().sorted(EXPRESSION_ORDER).toList();
  }

  /**
   * Factors conjunction operands common to every branch of a disjunction. Factoring is deliberately
   * limited to branches that are all conjunctions and retain at least one operand after factoring.
   */
  private static Expression factorCommonConjuncts(List<Expression> disjuncts) {
    if (disjuncts.size() < 2
        || disjuncts.stream()
            .anyMatch(
                expression ->
                    !And.ID.equals(expression.getFunctionId())
                        || expression.getArguments() == null
                        || expression.getArguments().size() < 2)) {
      return null;
    }

    List<Map<String, Expression>> conjunctsByBranch =
        disjuncts.stream()
            .map(expression -> byStructuralKey(expression.getArguments()))
            .toList();
    Set<String> commonKeys = new LinkedHashSet<>(conjunctsByBranch.get(0).keySet());
    conjunctsByBranch.subList(1, conjunctsByBranch.size()).stream()
        .map(Map::keySet)
        .forEach(commonKeys::retainAll);
    if (commonKeys.isEmpty()) return null;
    if (conjunctsByBranch.stream().anyMatch(branch -> branch.size() == commonKeys.size())) {
      return null;
    }

    List<Expression> common =
        commonKeys.stream().sorted().map(conjunctsByBranch.get(0)::get).toList();
    List<Expression> remainders =
        conjunctsByBranch.stream()
            .map(
                branch -> {
                  List<Expression> remaining =
                      branch.entrySet().stream()
                          .filter(entry -> !commonKeys.contains(entry.getKey()))
                          .map(Map.Entry::getValue)
                          .toList();
                  return remaining.size() == 1 ? remaining.get(0) : And.of(remaining);
                })
            .toList();

    Expression alternatives = remainders.size() == 1 ? remainders.get(0) : Or.of(remainders);
    List<Expression> factoredOperands = new ArrayList<>(common);
    factoredOperands.add(alternatives);
    return And.of(factoredOperands);
  }

  private static Map<String, Expression> byStructuralKey(List<Expression> expressions) {
    Map<String, Expression> byKey = new LinkedHashMap<>();
    distinctAndOrdered(expressions)
        .forEach(expression -> byKey.put(structuralKey(expression), expression));
    return byKey;
  }

  private static Expression copyWithArguments(
      Expression expression, List<Expression> arguments) {
    return new Expression()
        .functionId(expression.getFunctionId())
        .type(expression.getType())
        .entityId(expression.getEntityId())
        .constantId(expression.getConstantId())
        .arguments(arguments)
        .values(expression.getValues())
        .restriction(expression.getRestriction());
  }

  private static String sortKey(Expression expression) {
    if (expression.getEntityId() != null) return "0:entity:" + expression.getEntityId();
    if (expression.getConstantId() != null) return "1:constant:" + expression.getConstantId();
    if (expression.getValues() != null) return "2:values:" + expression.getValues();
    String functionId = expression.getFunctionId();
    if (functionId != null) {
      String arguments =
          expression.getArguments() == null
              ? ""
              : expression.getArguments().stream()
                  .map(QueryExpansionExpressionNormalizer::sortKey)
                  .reduce("", (left, right) -> left + "[" + right + "]");
      return "3:function:" + functionId + arguments;
    }
    return "4:expression:" + structuralKey(expression);
  }

  private static String structuralKey(Expression expression) {
    StringBuilder key = new StringBuilder();
    appendPart(key, expression.getFunctionId());
    appendPart(key, expression.getType());
    appendPart(key, expression.getEntityId());
    appendPart(key, expression.getConstantId());
    appendPart(key, expression.getValues());
    appendPart(key, expression.getRestriction());
    List<Expression> arguments = expression.getArguments();
    if (arguments == null) {
      key.append("A-;");
    } else {
      key.append('A').append(arguments.size()).append('[');
      arguments.forEach(argument -> appendPart(key, structuralKey(argument)));
      key.append(']');
    }
    return key.toString();
  }

  private static void appendPart(StringBuilder target, Object value) {
    if (value == null) {
      target.append("-;");
      return;
    }
    String text = value.toString();
    target.append(text.length()).append(':').append(text).append(';');
  }
}
