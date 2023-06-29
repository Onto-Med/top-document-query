package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Expression;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.Expressions;
import java.time.LocalDateTime;

public class DateTimeRange extends Range {

  private LocalDateTime minValue;
  private LocalDateTime maxValue;

  public DateTimeRange() {}

  public DateTimeRange(Quantifier quantifier) {
    super(quantifier);
  }

  public DateTimeRange(Quantifier quantifier, Integer cardinality) {
    super(quantifier, cardinality);
  }

  public DateTimeRange limit(Expression oper, Expression val) {
    return limit(getOperator(oper), Expressions.getDateTimeValue(val));
  }

  public DateTimeRange limit(RestrictionOperator oper, String val) {
    return limit(oper, DateUtil.parse(val));
  }

  public DateTimeRange limit(RestrictionOperator oper, LocalDateTime val) {
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

  public DateTimeRestriction get() {
    DateTimeRestriction r =
        (DateTimeRestriction)
            new DateTimeRestriction()
                .minOperator(minOperator)
                .maxOperator(maxOperator)
                .type(DataType.DATE_TIME)
                .cardinality(cardinality)
                .quantifier(quantifier);

    if (hasMax()) r.addValuesItem(minValue).addValuesItem(maxValue);
    else if (hasMin()) r.addValuesItem(minValue);

    return r;
  }
}
