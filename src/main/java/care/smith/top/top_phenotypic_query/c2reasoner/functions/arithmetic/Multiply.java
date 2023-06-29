package care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.math.BigDecimal;
import java.util.List;

public class Multiply extends FunctionEntity {

  private static Multiply INSTANCE = new Multiply();

  private Multiply() {
    super("*", NotationEnum.INFIX, 2, 2);
  }

  public static Multiply get() {
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
    args = c2r.calculate(args);
    if (args == null) return null;
    Exceptions.checkArgumentsType(getFunction(), DataType.NUMBER, args);
    args = Aggregator.aggregate(args, c2r);
    BigDecimal arg1 = Expressions.getNumberValue(args.get(0));
    BigDecimal arg2 = Expressions.getNumberValue(args.get(1));
    BigDecimal mul = arg1.multiply(arg2, c2r.getMathContext());
    return Exp.of(mul);
  }
}
