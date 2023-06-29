package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringConstant;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;

public class LtC extends ConstantEntity {

  private static final LtC INSTANCE = new LtC();

  private LtC() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new StringConstant().id("lt").title("<").dataType(DataType.STRING);
  }

  private static Value createValue() {
    return new StringValue()
        .value(RestrictionOperator.LESS_THAN.getValue())
        .dataType(DataType.STRING);
  }

  public static LtC get() {
    return INSTANCE;
  }
}
