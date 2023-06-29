package care.smith.top.top_phenotypic_query.c2reasoner.functions.bool;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class MinTrue extends FunctionEntity {

  private static MinTrue INSTANCE = new MinTrue();

  private MinTrue() {
    super("minTrue", NotationEnum.PREFIX);
    minArgumentNumber(2);
  }

  public static MinTrue get() {
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
    Exceptions.checkArgumentHasValueOfType(getFunction(), DataType.NUMBER, args.get(0));
    int min = Expressions.getNumberValue(args.get(0)).intValue();
    int count = 0;
    for (Expression arg : args.subList(1, args.size())) {
      arg = c2r.calculate(arg);
      if (arg == null) continue;
      arg = Aggregator.aggregate(arg, c2r);
      Exceptions.checkArgumentHasValueOfType(getFunction(), DataType.BOOLEAN, arg);
      if (Expressions.hasValueTrue(arg)) {
        count++;
        if (count >= min) return Exp.ofTrue();
      }
    }
    return Exp.ofFalse();
  }
}
