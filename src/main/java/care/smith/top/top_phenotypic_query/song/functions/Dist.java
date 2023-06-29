package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.util.builder.Exp;

public abstract class Dist extends TextFunction {

  public static final String ID = "Dist";
  private static final NotationEnum NOTATION = NotationEnum.PREFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction()
          .id(ID)
          .title(ID)
          .notation(NOTATION)
          .minArgumentNumber(2)
          .maxArgumentNumber(2);

  protected Dist() {
    super(ID, ID, NOTATION);
  }

  public static Expression of(Expression arg, int dist) {
    return Exp.function(ID, arg, Exp.of(dist));
  }

  public static Expression of(Entity arg, int dist) {
    return of(Exp.of(arg), dist);
  }

  public static Expression of(String phenotypeId, int dist) {
    return of(Exp.ofEntity(phenotypeId), dist);
  }
}
