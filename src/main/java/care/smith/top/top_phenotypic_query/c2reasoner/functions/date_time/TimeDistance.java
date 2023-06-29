package care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.NumberRange;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TimeDistance extends FunctionEntity {

  private static TimeDistance INSTANCE = new TimeDistance();

  private TimeDistance() {
    super("timeDistance", NotationEnum.PREFIX, 3, 6);
  }

  public static TimeDistance get() {
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
    if (args == null) return Exp.ofFalse();
    List<Value> vals = args.get(0).getValues();
    if (vals.size() < 2) return Exp.ofFalse();
    vals = vals.stream().sorted(Values.VALUE_DATE_COMPARATOR).collect(Collectors.toList());

    int a = 1;
    Expression arg2 = args.get(1);
    BigDecimal count = BigDecimal.valueOf(2);
    if (Expressions.hasNumberType(arg2)) {
      count = Expressions.getNumberValue(arg2);
      if (count.intValue() < 2) return Exp.ofFalse();
      a = 2;
    }
    if (vals.size() < count.intValue()) return Exp.ofFalse();

    NumberRange nr = new NumberRange();

    for (int i = a; i < args.size(); i += 2) {
      Expression oper = args.get(i);
      Exceptions.checkArgumentTypes(getFunction(), oper, DataType.STRING);
      Expression val = args.get(i + 1);
      Exceptions.checkArgumentTypes(getFunction(), val, DataType.NUMBER);
      nr.limit(oper, val);
    }

    int k = 1;
    for (int i = 1; i < vals.size(); i++) {
      if (checkValue(vals.get(i - 1), vals.get(i), nr)) {
        k++;
        if (k == count.intValue()) return Exp.ofTrue();
      } else k = 1;
    }
    return Exp.ofFalse();
  }

  private boolean checkValue(Value v1, Value v2, NumberRange nr) {
    BigDecimal distance =
        DateUtil.getPeriodInHours(Values.getEndDateTime(v1), Values.getStartDateTime(v2));
    return Values.contains(Restrictions.getInterval(nr.get()), Val.of(distance));
  }
}
