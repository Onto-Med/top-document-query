package care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class Overlap2 extends FunctionEntity {

  private static Overlap2 INSTANCE = new Overlap2();

  private Overlap2() {
    super("overlap2", NotationEnum.PREFIX, 2, 4);
  }

  public static Overlap2 get() {
    return INSTANCE;
  }

  public static Expression of(List<Expression> args) {
    return Exp.function(get().getClass().getSimpleName(), args);
  }

  public static Expression of(Expression... args) {
    return of(List.of(args));
  }

  @Override
  public Expression calculate(List<Expression> args, C2R c2r) {
    Exceptions.checkArgumentsNumber(getFunction(), args);
    Expression arg1 = c2r.calculate(args.get(0));
    if (arg1 == null) return null;
    Expression arg2 = c2r.calculate(args.get(1));
    if (arg2 == null) return null;

    Value v1 = Expressions.getValue(Aggregator.aggregate(arg1, c2r));
    Value v2 = Expressions.getValue(Aggregator.aggregate(arg2, c2r));

    int distance1 = 0;
    int distance2 = 0;
    if (args.size() > 2) {
      Expression arg3 = c2r.calculate(args.get(2));
      Exceptions.checkArgumentType(getFunction(), DataType.NUMBER, arg3);
      distance1 = Expressions.getNumberValue(arg3).intValue();
      if (args.size() > 3) {
        Expression arg4 = c2r.calculate(args.get(3));
        Exceptions.checkArgumentType(getFunction(), DataType.NUMBER, arg4);
        distance2 = Expressions.getNumberValue(arg4).intValue();
      } else distance2 = distance1;
    }

    return Exp.of(Values.overlaps2(v1, v2, distance1, distance2));
  }
}
