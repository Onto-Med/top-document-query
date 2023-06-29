package care.smith.top.top_phenotypic_query.c2reasoner.functions.set;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Quantifier;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.Exceptions;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Aggregator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;
import java.util.Map;

public class In extends FunctionEntity {

  private static final In INSTANCE = new In();

  private In() {
    super("in", NotationEnum.PREFIX, 2, 2);
  }

  public static In get() {
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
    Exceptions.checkArgumentsHaveSameType(getFunction(), args);

    if (args.get(1).getValues() != null) {
      Expression val = Aggregator.aggregate(args.get(0), c2r);
      return Exp.of(Values.contains(args.get(1).getValues(), Expressions.getValue(val)));
    }

    Restriction r = args.get(1).getRestriction();
    List<Value> vals = args.get(0).getValues();
    if (Restrictions.hasInterval(r))
      return calculateInInterval(
          vals, Restrictions.getInterval(r), r.getQuantifier(), r.getCardinality());
    return calculateInSet(vals, Restrictions.getValues(r), r.getQuantifier(), r.getCardinality());
  }

  private Expression calculateInInterval(
      List<Value> vals, Map<RestrictionOperator, Value> inter, Quantifier quan, Integer card) {
    int hits = 0;
    for (Value v : vals) if (Values.contains(inter, v)) hits++;
    return Exp.of(checkQuantifier(vals.size(), hits, quan, card));
  }

  private Expression calculateInSet(
      List<Value> vals, List<Value> set, Quantifier quan, Integer card) {
    int hits = 0;
    for (Value v : vals) if (Values.contains(set, v)) hits++;
    return Exp.of(checkQuantifier(vals.size(), hits, quan, card));
  }

  private boolean checkQuantifier(int size, int hits, Quantifier quan, Integer card) {
    if (quan == Quantifier.ALL && hits == size) return true;
    if (quan == Quantifier.EXACT && card != null && hits == card.intValue()) return true;
    if (quan == Quantifier.MIN && card != null && hits >= card.intValue()) return true;
    if (quan == Quantifier.MAX && card != null && hits <= card.intValue()) return true;
    return false;
  }
}
