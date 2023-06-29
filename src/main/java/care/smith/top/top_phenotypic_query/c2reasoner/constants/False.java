package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.BooleanConstant;
import care.smith.top.model.BooleanValue;
import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.Value;

public class False extends ConstantEntity {

  private static final False INSTANCE = new False();

  private False() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new BooleanConstant().id("false").title("false").dataType(DataType.BOOLEAN);
  }

  private static Value createValue() {
    return new BooleanValue().value(false).dataType(DataType.BOOLEAN);
  }

  public static False get() {
    return INSTANCE;
  }
}
