package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.Expression;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.util.Expressions;

public class Range {

  protected RestrictionOperator minOperator;
  protected RestrictionOperator maxOperator;
  protected Integer cardinality;
  protected Quantifier quantifier;

  protected Range() {}

  protected Range(Quantifier quantifier) {
    this.quantifier = quantifier;
  }

  protected Range(Quantifier quantifier, Integer cardinality) {
    this.quantifier = quantifier;
    this.cardinality = cardinality;
  }

  protected RestrictionOperator getOperator(Expression exp) {
    if (exp == null) return null;
    String val = Expressions.getStringValue(exp);
    if (val == null) return null;
    return RestrictionOperator.fromValue(val);
  }
}
