package care.smith.top.top_document_query.query_expansion;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.functions.XProd;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.Optional;

public class QueryExpansionExpressionCompiler {
  private static final int DEFAULT_PROXIMITY_DISTANCE = 5;

  private QueryExpansionExpressionCompiler() {}

  public static Optional<Expression> compileRelation(
      String strategy, String sourceEntityId, String targetEntityId) {
    return compileRelation(QueryExpansionStrategy.fromId(strategy), sourceEntityId, targetEntityId);
  }

  public static Optional<Expression> compileRelation(
      QueryExpansionStrategy strategy, String sourceEntityId, String targetEntityId) {
    Expression source = Exp.ofEntity(sourceEntityId);
    Expression target = Exp.ofEntity(targetEntityId);

    return switch (strategy) {
      case ALTERNATIVES, OPTIONAL_CONTEXT, RECALL_EXPANSION -> Optional.of(Or.of(source, target));
      case REQUIRE_CONTEXT, SPECIFICITY_FOCUS -> Optional.of(And.of(source, target));
      case EXCLUDE_CONTEXT -> Optional.of(And.of(source, Not.of(target)));
      case PROXIMITY_CONTEXT ->
          Optional.of(Dist.of(XProd.of(source, target), DEFAULT_PROXIMITY_DISTANCE));
      case PHRASE_CONTEXT -> Optional.of(XProd.of(source, target));
      case IGNORE -> Optional.empty();
    };
  }
}
