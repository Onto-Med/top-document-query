package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringConstant;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;

public class GtC extends ConstantEntity {

  private static final GtC INSTANCE = new GtC();

  private GtC() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new StringConstant().id("gt").title(">").dataType(DataType.STRING);
  }

  private static Value createValue() {
    return new StringValue()
        .value(RestrictionOperator.GREATER_THAN.getValue())
        .dataType(DataType.STRING);
  }

  public static GtC get() {
    return INSTANCE;
  }
}
