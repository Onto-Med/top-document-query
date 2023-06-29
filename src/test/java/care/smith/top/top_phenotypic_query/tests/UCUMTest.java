package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.top_phenotypic_query.ucum.UCUM;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

public class UCUMTest extends AbstractTest {

  @Test
  public void test1() {
    assertEquals(
        BigDecimal.valueOf(1.8),
        UCUM.convert(BigDecimal.valueOf(180), "cm", "m").setScale(1, RoundingMode.HALF_UP));
  }

  @Test
  public void test2() {
    assertEquals(
        BigDecimal.valueOf(180.0),
        UCUM.convert(BigDecimal.valueOf(1.8), "m", "cm").setScale(1, RoundingMode.HALF_UP));
  }

  @Test
  public void test3() {
    assertEquals(
        BigDecimal.valueOf(1.8),
        UCUM.convert(BigDecimal.valueOf(1.8), "m", null).setScale(1, RoundingMode.HALF_UP));
  }
}
