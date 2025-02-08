package care.smith.top.top_document_query.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.List;
import java.util.stream.Collectors;

public class SubTree extends TextFunction {

  public static final String ID = "SubTree";
  private static final NotationEnum NOTATION = NotationEnum.PREFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction()
          .id(ID)
          .title(ID)
          .notation(NOTATION)
          .minArgumentNumber(1)
          .maxArgumentNumber(2);

  private static SubTree INSTANCE = new SubTree();

  private SubTree() {
    super(ID, ID, NOTATION);
  }

  public static SubTree get() {
    return INSTANCE;
  }

  public static Expression of(Expression arg) {
    return Exp.function(ID, arg);
  }

  public static Expression of(Entity arg) {
    return of(Exp.of(arg));
  }

  public static Expression of(String conceptId) {
    return of(Exp.ofEntity(conceptId));
  }

  public static Expression of(Expression arg, int level) {
    return Exp.function(ID, arg, Exp.of(level));
  }

  public static Expression of(Entity arg, int level) {
    return of(Exp.of(arg), level);
  }

  public static Expression of(String conceptId, int level) {
    return of(Exp.ofEntity(conceptId), level);
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    if (args == null || args.isEmpty()) return new Expression();
    Expression arg = args.get(0);
    int subTreeLevel = -1;
    try {
      subTreeLevel = args.size() == 2 ? Expressions.getNumberValue(args.get(1)).intValue() : -1;
    } catch (Exception e) {
      LOGGER.warning(
          String.format(
              "Encountered error when getting NumberValue for '%s'. Using -1 as distance",
              args.get(1)));
    }
    if (arg.getEntityId() == null) return new Expression();
    String query =
        Expressions.getStringValues(
                song.getTermsExpression(
                    arg.getEntityId(), SONG.EXPRESSION_TYPE_TERMS_INITIAL, subTreeLevel))
            .stream()
            .map(s -> s.split("\\s+").length > 1 ? String.format("\"%s\"", s) : s)
            .collect(Collectors.joining(" OR "));
    return Exp.of("(" + query + ")").type(SONG.EXPRESSION_TYPE_QUERY);
  }
}
