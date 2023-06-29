package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public abstract class And extends TextFunction {

  public static final String ID = "AND";
  private static final NotationEnum NOTATION = NotationEnum.INFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction().id(ID).title(ID).notation(NOTATION).minArgumentNumber(2);

  protected And() {
    super(ID, ID, NOTATION);
  }

  public static Expression of(List<Expression> args) {
    return Exp.function(ID, args);
  }

  public static Expression of(Expression... args) {
    return of(List.of(args));
  }

  public static Expression of(Entity... args) {
    return of(Exp.toList(args));
  }
}
