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

public class Not extends FunctionEntity {

  private static Not INSTANCE = new Not();

  private Not() {
    super("not", NotationEnum.PREFIX, 1, 1);
  }

  public static Not get() {
    return INSTANCE;
  }

  public static Expression of(Expression arg) {
    return Exp.function(get().getClass().getSimpleName(), arg);
  }

  public static Expression of(Phenotype arg) {
    return of(Exp.of(arg));
  }

  public static Expression of(String phenotypeId) {
    return of(Exp.ofEntity(phenotypeId));
  }

  @Override
  public Expression calculate(List<Expression> args, C2R c2r) {
    Exceptions.checkArgumentsNumber(getFunction(), args);
    Expression arg = c2r.calculate(args.get(0));
    if (arg == null) return Exp.ofTrue();
    arg = Aggregator.aggregate(arg, c2r);
    Exceptions.checkArgumentHasValueOfType(getFunction(), DataType.BOOLEAN, arg);
    if (Expressions.hasValueTrue(arg)) return Exp.ofFalse();
    return Exp.ofTrue();
  }
}
