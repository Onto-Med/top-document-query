package care.smith.top.top_phenotypic_query.c2reasoner.functions.set;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.ArrayList;
import java.util.List;

public class Union extends FunctionEntity {

  private static final Union INSTANCE = new Union();

  private Union() {
    super("union", NotationEnum.PREFIX);
    minArgumentNumber(2);
  }

  public static Union get() {
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
    List<Value> vals = new ArrayList<>();
    for (Expression arg : args) {
      arg = c2r.calculate(arg);
      if (arg != null && arg.getValues() != null) vals.addAll(arg.getValues());
    }
    return Exp.of(vals);
  }
}
