package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Expression;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.TimeDistance;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import org.junit.jupiter.api.Test;

public class TimeDistanceTest {

  @Test
  public void test1() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-03"));
    Value v3 = Val.of(7, DateUtil.parse("2001-01-04"));

    Expression e =
        TimeDistance.of(
            Exp.of(v1, v2, v3), Exp.ofConstant("ge"), Exp.of(2), Exp.ofConstant("lt"), Exp.of(24));
    assertEquals(Exp.ofFalse(), new C2R().calculate(e));

    e =
        TimeDistance.of(
            Exp.of(v1, v2, v3), Exp.ofConstant("ge"), Exp.of(2), Exp.ofConstant("le"), Exp.of(24));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));

    e =
        TimeDistance.of(
            Exp.of(v1, v2, v3), Exp.ofConstant("ge"), Exp.of(25), Exp.ofConstant("le"), Exp.of(48));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));

    e = TimeDistance.of(Exp.of(v1, v2, v3), Exp.ofConstant("gt"), Exp.of(48));
    assertEquals(Exp.ofFalse(), new C2R().calculate(e));

    e = TimeDistance.of(Exp.of(v1, v2, v3), Exp.ofConstant("ge"), Exp.of(48));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));
  }

  @Test
  public void test2() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-03"));
    Value v3 = Val.of(7, DateUtil.parse("2001-01-04"));

    Expression e =
        TimeDistance.of(
            Exp.of(v1, v2, v3),
            Exp.of(3),
            Exp.ofConstant("ge"),
            Exp.of(2),
            Exp.ofConstant("le"),
            Exp.of(24));
    assertEquals(Exp.ofFalse(), new C2R().calculate(e));

    e =
        TimeDistance.of(
            Exp.of(v1, v2, v3),
            Exp.of(3),
            Exp.ofConstant("gt"),
            Exp.of(24),
            Exp.ofConstant("le"),
            Exp.of(48));
    assertEquals(Exp.ofFalse(), new C2R().calculate(e));

    e = TimeDistance.of(Exp.of(v1, v2, v3), Exp.of(3), Exp.ofConstant("ge"), Exp.of(48));
    assertEquals(Exp.ofFalse(), new C2R().calculate(e));

    e = TimeDistance.of(Exp.of(v1, v2, v3), Exp.of(3), Exp.ofConstant("ge"), Exp.of(24));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));

    e = TimeDistance.of(Exp.of(v1, v2, v3), Exp.of(3), Exp.ofConstant("le"), Exp.of(48));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));

    e =
        TimeDistance.of(
            Exp.of(v1, v2, v3),
            Exp.of(3),
            Exp.ofConstant("ge"),
            Exp.of(24),
            Exp.ofConstant("le"),
            Exp.of(48));
    assertEquals(Exp.ofTrue(), new C2R().calculate(e));
  }
}
