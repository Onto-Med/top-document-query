package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.EndsBefore;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.Overlap1;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.Overlap2;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.StartsBefore;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import org.junit.jupiter.api.Test;

public class PeriodTest {

  @Test
  public void test1() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(7, DateUtil.parse("2001-01-02"));
    assertEquals(Exp.ofTrue(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v3))));
  }

  @Test
  public void test2() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-02"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test3() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-02"), DateUtil.parse("2001-01-03"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test4() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-02"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test5() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test6() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-03"), DateUtil.parse("2001-01-04"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap1.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test7() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-03"), DateUtil.parse("2001-01-04"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test8() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"), DateUtil.parse("2001-01-04"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(24))));
  }

  @Test
  public void test9() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"), DateUtil.parse("2001-01-04"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    assertEquals(
        Exp.ofFalse(),
        new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(24), Exp.of(23))));
    assertEquals(
        Exp.ofTrue(),
        new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(23), Exp.of(24))));
  }

  @Test
  public void test10() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    assertEquals(
        Exp.ofFalse(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(47))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(48))));
    assertEquals(
        Exp.ofTrue(), new C2R().calculate(Overlap2.of(Exp.of(v1), Exp.of(v2), Exp.of(148))));
  }

  @Test
  public void test11() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    assertEquals(Exp.ofFalse(), new C2R().calculate(StartsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(StartsBefore.of(Exp.of(v2), Exp.of(v1))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(EndsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(EndsBefore.of(Exp.of(v2), Exp.of(v1))));
  }

  @Test
  public void test12() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-03"), DateUtil.parse("2001-01-04"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    assertEquals(Exp.ofFalse(), new C2R().calculate(StartsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(StartsBefore.of(Exp.of(v2), Exp.of(v1))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(EndsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(EndsBefore.of(Exp.of(v2), Exp.of(v1))));
  }

  @Test
  public void test13() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-02"), DateUtil.parse("2001-01-03"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-04"));
    assertEquals(Exp.ofFalse(), new C2R().calculate(StartsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(StartsBefore.of(Exp.of(v2), Exp.of(v1))));
    assertEquals(Exp.ofTrue(), new C2R().calculate(EndsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(EndsBefore.of(Exp.of(v2), Exp.of(v1))));
  }

  @Test
  public void test14() {
    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"), DateUtil.parse("2001-01-02"));
    assertEquals(Exp.ofFalse(), new C2R().calculate(StartsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(StartsBefore.of(Exp.of(v2), Exp.of(v1))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(EndsBefore.of(Exp.of(v1), Exp.of(v2))));
    assertEquals(Exp.ofFalse(), new C2R().calculate(EndsBefore.of(Exp.of(v2), Exp.of(v1))));
  }
}
