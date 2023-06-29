package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.util.List;

public class XProd extends TextFunction {

  public static final String ID = "XProd";
  private static final NotationEnum NOTATION = NotationEnum.PREFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction()
          .id(ID)
          .title(ID)
          .notation(NOTATION)
          .minArgumentNumber(2)
          .maxArgumentNumber(2);

  private static XProd INSTANCE = new XProd();

  private XProd() {
    super(ID, ID, NOTATION);
  }

  public static XProd get() {
    return INSTANCE;
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

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    if (args == null || args.isEmpty()) return new Expression();
    if (args.size() == 1) return args.get(0);
    args = song.generate(args);
    if (args.size() == 1) return args.get(0);

    Expression xProds = new Expression().type(SONG.EXPRESSION_TYPE_TERMS_PROCESSED);

    for (Value v1 : args.get(0).getValues()) {
      for (Value v2 : args.get(1).getValues()) {
        String xProd = "\"" + Values.getStringValue(v1) + " " + Values.getStringValue(v2) + "\"";
        xProds.addValuesItem(Val.of(xProd));
      }
    }

    return xProds;
  }
}
