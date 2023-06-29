package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringConstant;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;

public class GeC extends ConstantEntity {

  private static final GeC INSTANCE = new GeC();

  private GeC() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new StringConstant().id("ge").title(">=").dataType(DataType.STRING);
  }

  private static Value createValue() {
    return new StringValue()
        .value(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO.getValue())
        .dataType(DataType.STRING);
  }

  public static GeC get() {
    return INSTANCE;
  }
}
