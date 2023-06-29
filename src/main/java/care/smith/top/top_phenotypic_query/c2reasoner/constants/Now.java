package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeConstant;
import care.smith.top.model.DateTimeValue;
import care.smith.top.model.Value;
import java.time.LocalDateTime;

public class Now extends ConstantEntity {

  private static final Now INSTANCE = new Now();

  private Now() {
    super(createConstant(), null);
  }

  private static Constant createConstant() {
    return new DateTimeConstant().id("now").title("now").dataType(DataType.DATE_TIME);
  }

  @Override
  public Value getValue() {
    return new DateTimeValue().value(LocalDateTime.now()).dataType(DataType.DATE_TIME);
  }

  public static Now get() {
    return INSTANCE;
  }
}
