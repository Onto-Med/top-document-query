package care.smith.top.top_phenotypic_query.c2reasoner.functions.bool;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class And extends FunctionEntity {

  private static And INSTANCE = new And();

  private And() {
    super("and", NotationEnum.PREFIX);
    minArgumentNumber(2);
  }

  public static And get() {
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
    for (Expression arg : args) {
      arg = c2r.calculate(arg);
      if (arg == null) return Exp.ofFalse();
      arg = Aggregator.aggregate(arg, c2r);
      Exceptions.checkArgumentHasValueOfType(getFunction(), DataType.BOOLEAN, arg);
      if (Expressions.hasValueFalse(arg)) return arg;
    }
    return Exp.ofTrue();
  }
}
