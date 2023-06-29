package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.BooleanConstant;
import care.smith.top.model.BooleanValue;
import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.Value;

public class True extends ConstantEntity {

  private static final True INSTANCE = new True();

  private True() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new BooleanConstant().id("true").title("true").dataType(DataType.BOOLEAN);
  }

  private static Value createValue() {
    return new BooleanValue().value(true).dataType(DataType.BOOLEAN);
  }

  public static True get() {
    return INSTANCE;
  }
}
