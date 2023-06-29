package care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class StartsBefore extends FunctionEntity {

  private static StartsBefore INSTANCE = new StartsBefore();

  private StartsBefore() {
    super("startsBefore", NotationEnum.PREFIX, 2, 2);
  }

  public static StartsBefore get() {
    return INSTANCE;
  }

  public static Expression of(List<Expression> args) {
    return Exp.function(get().getClass().getSimpleName(), args);
  }

  public static Expression of(Expression... args) {
    return of(List.of(args));
  }

  public static Expression of(Phenotype... args) {
    return of(Exp.toList(args));
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

    return Exp.of(Values.startsBefore(v1, v2));
  }
}
