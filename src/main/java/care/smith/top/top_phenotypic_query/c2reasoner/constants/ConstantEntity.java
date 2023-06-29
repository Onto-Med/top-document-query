package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.Expression;
import care.smith.top.model.Value;

public class ConstantEntity {

  private Constant constant;
  private Value value;

  protected ConstantEntity(Constant constant, Value value) {
    this.constant = constant;
    this.value = value;
  }

  public Constant getConstant() {
    return constant;
  }

  public Value getValue() {
    return value;
  }

  public Expression getValueExpression() {
    return new Expression().addValuesItem(getValue());
  }
}
