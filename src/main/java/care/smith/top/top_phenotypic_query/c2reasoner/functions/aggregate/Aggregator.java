package care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate;

import care.smith.top.model.Expression;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aggregator {

  private static Logger log = LoggerFactory.getLogger(Aggregator.class);

  public static Expression aggregate(Expression arg, C2R c2r) {
    if (!hasMultipleValues(arg)) return arg;
    Expression ag = c2r.getDefaultAggregateFunction().calculate(Exp.toList(arg.getValues()), c2r);
    log.debug(
        "aggregate: {} = {}",
        c2r.getDefaultAggregateFunction().toStringValues(arg.getValues()),
        Values.toStringWithoutDateTime(Expressions.getValue(ag)));
    return ag;
  }

  public static List<Expression> aggregate(List<Expression> args, C2R c2r) {
    if (containsMultipleValues(args))
      return args.stream().map(a -> aggregate(a, c2r)).collect(Collectors.toList());
    return args;
  }

  public static List<Expression> aggregateIfNumber(List<Expression> args, C2R c2r) {
    if (containsMultipleValues(args))
      return args.stream()
          .map(
              a -> {
                if (Expressions.hasNumberType(a)) return aggregate(a, c2r);
                else return a;
              })
          .collect(Collectors.toList());
    return args;
  }

  public static List<Expression> aggregateIfMultiple(List<Expression> args, C2R c2r) {
    if (args.size() > 1) return aggregate(args, c2r);
    return Exp.toList(args.get(0).getValues());
  }

  private static boolean containsMultipleValues(List<Expression> args) {
    for (Expression arg : args) if (hasMultipleValues(arg)) return true;
    return false;
  }

  private static boolean hasMultipleValues(Expression arg) {
    return arg.getValues() != null && arg.getValues().size() > 1;
  }
}
