package care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class Switch extends FunctionEntity {

  private static Switch INSTANCE = new Switch();

  private Switch() {
    super("switch", NotationEnum.PREFIX);
    minArgumentNumber(2);
  }

  public static Switch get() {
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
    if (args.size() % 2 == 0) return calculate(args, args.size(), null, c2r);
    return calculate(args, args.size() - 1, args.get(args.size() - 1), c2r);
  }

  private Expression calculate(
      List<Expression> args, int lastValueNum, Expression defaultValue, C2R c2r) {
    for (int i = 0; i < lastValueNum; i += 2) {
      Expression cond = c2r.calculate(args.get(i));
      Exceptions.checkArgumentType(getFunction(), DataType.BOOLEAN, cond);
      if (Expressions.getBooleanValue(cond)) return c2r.calculate(args.get(i + 1));
    }
    if (defaultValue != null) {
      defaultValue = c2r.calculate(defaultValue);
      if (defaultValue != null) return defaultValue;
    }
    throw new ArithmeticException("No default value defined for the function 'switch'!");
  }
}
