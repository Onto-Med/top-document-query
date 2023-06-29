package care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;
import java.util.stream.Collectors;

public class Last extends FunctionEntity {

  private static final Last INSTANCE = new Last();

  private Last() {
    super("last", NotationEnum.PREFIX);
    minArgumentNumber(1);
  }

  public static Last get() {
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
    args = c2r.calculate(args);
    if (args == null) return null;
    args = Aggregator.aggregateIfMultiple(args, c2r);
    args = args.stream().sorted(Values.EXP_DATE_COMPARATOR).collect(Collectors.toList());
    return args.get(args.size() - 1);
  }
}
