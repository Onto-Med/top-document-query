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

public class Vals extends FunctionEntity {

  private static Vals INSTANCE = new Vals();

  private Vals() {
    super("values", NotationEnum.PREFIX);
    minArgumentNumber(1);
  }

  public static Vals get() {
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
      if (arg.getEntityId() == null) continue;
      List<Value> argVals = c2r.getValues(arg.getEntityId());
      if (argVals != null) vals.addAll(argVals);
    }
    return Exp.of(vals);
  }
}
