package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.util.builder.Exp;

public abstract class Not extends TextFunction {

  public static final String ID = "NOT";
  private static final NotationEnum NOTATION = NotationEnum.PREFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction()
          .id(ID)
          .title(ID)
          .notation(NOTATION)
          .minArgumentNumber(1)
          .maxArgumentNumber(1);

  protected Not() {
    super(ID, ID, NOTATION);
  }

  public static Expression of(Expression arg) {
    return Exp.function(ID, arg);
  }

  public static Expression of(Entity arg) {
    return of(Exp.of(arg));
  }

  public static Expression of(String entityId) {
    return of(Exp.ofEntity(entityId));
  }
}
