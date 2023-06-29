package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.NumberConstant;
import care.smith.top.model.NumberValue;
import care.smith.top.model.Value;
import java.math.BigDecimal;

public class Pi extends ConstantEntity {

  private static final Pi INSTANCE = new Pi();

  private Pi() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new NumberConstant().id("pi").title("pi").dataType(DataType.NUMBER);
  }

  private static Value createValue() {
    return new NumberValue()
        .value(
            new BigDecimal(
                "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679"))
        .dataType(DataType.NUMBER);
  }

  public static Pi get() {
    return INSTANCE;
  }
}
