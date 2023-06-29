package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.NumberRestriction;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Values;
import java.math.BigDecimal;

public class NumberRange extends Range {

  private BigDecimal minValue;
  private BigDecimal maxValue;

  public NumberRange() {}

  public NumberRange(Quantifier quantifier) {
    super(quantifier);
  }

  public NumberRange(Quantifier quantifier, Integer cardinality) {
    super(quantifier, cardinality);
  }

  public NumberRange limit(Expression oper, Expression val) {
    return limit(getOperator(oper), Expressions.getNumberValue(val));
  }

  public NumberRange limit(RestrictionOperator oper, Number val) {
    return limit(oper, Values.toDecimal(val));
  }

  public NumberRange limit(RestrictionOperator oper, BigDecimal val) {
    if (oper == null || val == null) return this;
    if (oper == RestrictionOperator.GREATER_THAN
        || oper == RestrictionOperator.GREATER_THAN_OR_EQUAL_TO) {
      minOperator = oper;
      minValue = val;
    } else {
      maxOperator = oper;
      maxValue = val;
    }
    return this;
  }

  public boolean hasMin() {
    return minOperator != null && minValue != null;
  }

  public boolean hasMax() {
    return maxOperator != null && maxValue != null;
  }

  public boolean isPresent() {
    return hasMin() || hasMax();
  }

  public NumberRestriction get() {
    NumberRestriction r =
        (NumberRestriction)
            new NumberRestriction()
                .minOperator(minOperator)
                .maxOperator(maxOperator)
                .type(DataType.NUMBER)
                .cardinality(cardinality)
                .quantifier(quantifier);

    if (hasMax()) r.addValuesItem(minValue).addValuesItem(maxValue);
    else if (hasMin()) r.addValuesItem(minValue);

    return r;
  }
}
