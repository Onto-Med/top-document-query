package care.smith.top.top_phenotypic_query.c2reasoner.functions.set;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.DateTimeRange;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.NumberRange;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Filter extends FunctionEntity {

  private static Filter INSTANCE = new Filter();

  private Filter() {
    super("filter", NotationEnum.PREFIX, 3, 9);
  }

  public static Filter get() {
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

    for (int i = 1; i < args.size(); i++) {
      if (i % 2 == 0)
        Exceptions.checkArgumentTypes(
            getFunction(), args.get(i), DataType.NUMBER, DataType.DATE_TIME);
      else Exceptions.checkArgumentTypes(getFunction(), args.get(i), DataType.STRING);
    }

    NumberRange nr = new NumberRange();
    DateTimeRange dr = new DateTimeRange();

    for (int i = 1; i < args.size(); i += 2) {
      Expression oper = args.get(i);
      Expression val = args.get(i + 1);
      if (Expressions.hasNumberType(val)) nr.limit(oper, val);
      if (Expressions.hasDateTimeType(val)) dr.limit(oper, val);
    }

    List<Value> vals = args.get(0).getValues();

    if (Expressions.hasDateTimeType(args.get(0))) {
      if (dr.isPresent()) vals = filterValue(vals, Restrictions.getInterval(dr.get()));
    } else {
      if (nr.isPresent()) vals = filterValue(vals, Restrictions.getInterval(nr.get()));
      if (dr.isPresent()) vals = filterDate(vals, Restrictions.getInterval(dr.get()));
    }

    return Exp.of(vals);
  }

  private List<Value> filterValue(List<Value> vals, Map<RestrictionOperator, Value> inter) {
    return vals.stream().filter(v -> Values.contains(inter, v)).collect(Collectors.toList());
  }

  private List<Value> filterDate(List<Value> vals, Map<RestrictionOperator, Value> inter) {
    return vals.stream()
        .filter(v -> Values.contains(inter, Val.of(v.getDateTime())))
        .collect(Collectors.toList());
  }
}
