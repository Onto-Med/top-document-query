package care.smith.top.top_phenotypic_query.c2reasoner.constants;

import care.smith.top.model.Constant;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeConstant;
import care.smith.top.model.DateTimeValue;
import care.smith.top.model.Value;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Today extends ConstantEntity {

  private static final Today INSTANCE = new Today();

  private Today() {
    super(createConstant(), null);
  }

  private static Constant createConstant() {
    return new DateTimeConstant().id("today").title("today").dataType(DataType.DATE_TIME);
  }

  @Override
  public Value getValue() {
    return new DateTimeValue()
        .value(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))
        .dataType(DataType.DATE_TIME);
  }

  public static Today get() {
    return INSTANCE;
  }
}
