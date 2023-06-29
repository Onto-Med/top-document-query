package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.NumberConstant;
import care.smith.top.model.NumberValue;
import care.smith.top.model.Value;
import java.math.BigDecimal;

public class E extends ConstantEntity {

  private static final E INSTANCE = new E();

  private E() {
    super(createConstant(), createValue());
  }

  private static Constant createConstant() {
    return new NumberConstant().id("e").title("e").dataType(DataType.NUMBER);
  }

  private static Value createValue() {
    return new NumberValue()
        .value(
            new BigDecimal(
                "2.71828182845904523536028747135266249775724709369995957496696762772407663"))
        .dataType(DataType.NUMBER);
  }

  public static E get() {
    return INSTANCE;
  }
}
