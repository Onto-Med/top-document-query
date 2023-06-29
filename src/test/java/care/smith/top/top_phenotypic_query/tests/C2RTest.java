package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.model.Expression;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.If;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.Switch;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Avg;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutFirst;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutLast;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.First;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Last;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Max;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Median;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Min;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Add;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Divide;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Multiply;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Power;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.MinTrue;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Or;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Eq;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Ge;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Gt;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Le;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Lt;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Ne;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.DiffYears;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusYears;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Filter;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.In;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Li;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Union;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class C2RTest {

  @Test
  public void testList1() {
    assertEquals(Exp.of(5, 6, 7), new C2R().calculate(Li.of(Exp.of(5), Exp.of(6), Exp.of(7))));

    Value v1 = Val.of(3, DateUtil.parse("2001-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v3 = Val.of(7, DateUtil.parse("2001-01-01"));
    Value v4 = Val.of(9, DateUtil.parse("2000-01-01"));
    Expression li = Li.of(Exp.of(v1, v2), Exp.of(v3, v4));

    assertEquals(Exp.of(v1, v3), new C2R().calculate(li));
    assertEquals(Exp.of(4, 8), new C2R().defaultAggregateFunction(Avg.get()).calculate(li));
  }

  @Test
  public void testList2() {
    Phenotype x = new Phe("x").number().get();
    Phenotype y = new Phe("y").number().get();
    Phenotype c = new Phe("c").expression(Li.of(x, y)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("x", null, Val.of(3));
    vals.addValue("x", null, Val.of(5));
    vals.addValue("y", null, Val.of(7));
    vals.addValue("y", null, Val.of(9));

    Entities phens = Entities.of(x, y, c);

    C2R c2r = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Avg.get());

    assertEquals(Exp.of(4, 8), c2r.calculate(c));
  }

  @Test
  public void testUnion1() {
    Phenotype x = new Phe("x").number().get();
    Phenotype y = new Phe("y").number().get();
    Phenotype c = new Phe("c").expression(Union.of(x, y)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("x", null, Val.of(3));
    vals.addValue("x", null, Val.of(5));
    vals.addValue("y", null, Val.of(7));
    vals.addValue("y", null, Val.of(9));

    Entities phens = Entities.of(x, y, c);

    C2R c2r = new C2R().phenotypes(phens).values(vals);

    assertEquals(Exp.of(3, 5, 7, 9), c2r.calculate(c));
  }

  @Test
  public void testUnion2() {
    Phenotype x = new Phe("x").number().get();
    Phenotype y = new Phe("y").number().get();
    Phenotype c = new Phe("c").expression(Union.of(x, y)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("x", null, Val.of(3));
    vals.addValue("x", null, Val.of(5));

    Entities phens = Entities.of(x, y, c);

    C2R c2r = new C2R().phenotypes(phens).values(vals);

    assertEquals(Exp.of(3, 5), c2r.calculate(c));
  }

  @Test
  public void testIf() {
    Phenotype t = new Phe("t").bool().get();
    Phenotype f = new Phe("f").bool().get();
    Phenotype x = new Phe("x").number().get();
    Phenotype y = new Phe("y").number().get();
    Phenotype c1 = new Phe("c1").expression(If.of(t, x, y)).get();
    Phenotype c2 = new Phe("c2").expression(If.of(f, x, y)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("t", null, Val.ofTrue());
    vals.addValue("f", null, Val.ofFalse());
    vals.addValue("x", null, Val.of(1));
    vals.addValue("y", null, Val.of(2));

    Entities phens = Entities.of(t, f, x, y, c1, c2);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(List.of(BigDecimal.valueOf(1)), Expressions.getNumberValues(c.calculate(c1)));
    assertEquals(List.of(BigDecimal.valueOf(2)), Expressions.getNumberValues(c.calculate(c2)));
  }

  @Test
  public void testMedian() {
    Phenotype a = new Phe("a").number().get();
    Phenotype b = new Phe("b").number().get();
    Phenotype c1 = new Phe("c1").expression(Median.of(a)).get();
    Phenotype c2 = new Phe("c2").expression(Median.of(b)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, Val.of(5));
    vals.addValue("a", null, Val.of(3));
    vals.addValue("a", null, Val.of(2));
    vals.addValue("a", null, Val.of(4));
    vals.addValue("b", null, Val.of(5));
    vals.addValue("b", null, Val.of(3));
    vals.addValue("b", null, Val.of(2));
    vals.addValue("b", null, Val.of(4));
    vals.addValue("b", null, Val.of(1));

    Entities phens = Entities.of(a, b, c1, c2);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(List.of(BigDecimal.valueOf(3.5)), Expressions.getNumberValues(c.calculate(c1)));
    assertEquals(List.of(BigDecimal.valueOf(3)), Expressions.getNumberValues(c.calculate(c2)));
  }

  @Test
  public void testFirst() {
    Value v1 = Val.of(3, DateUtil.parse("2002-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(7, DateUtil.parse("2004-01-01"));
    Value v4 = Val.of(9, DateUtil.parse("2003-01-01"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype x = new Phe("x").expression(First.of(a)).get();
    Phenotype y = new Phe("y").expression(CutFirst.of(a)).get();

    Entities phens = Entities.of(a, x, y);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(List.of(BigDecimal.valueOf(5)), Expressions.getNumberValues(c.calculate(x)));
    assertEquals(
        List.of(BigDecimal.valueOf(3), BigDecimal.valueOf(9), BigDecimal.valueOf(7)),
        Expressions.getNumberValues(c.calculate(y)));
  }

  @Test
  public void testLast() {
    Value v1 = Val.of(3, DateUtil.parse("2002-01-01"));
    Value v2 = Val.of(5, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(7, DateUtil.parse("2004-01-01"));
    Value v4 = Val.of(9, DateUtil.parse("2003-01-01"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype x = new Phe("x").expression(Last.of(a)).get();
    Phenotype y = new Phe("y").expression(CutLast.of(a)).get();

    Entities phens = Entities.of(a, x, y);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(List.of(BigDecimal.valueOf(7)), Expressions.getNumberValues(c.calculate(x)));
    assertEquals(
        List.of(BigDecimal.valueOf(5), BigDecimal.valueOf(3), BigDecimal.valueOf(9)),
        Expressions.getNumberValues(c.calculate(y)));
  }

  @Test
  public void testFilter1() {
    Value v1 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v2 = Val.of(10, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(15, DateUtil.parse("2002-01-01"));
    Value v4 = Val.of(8, DateUtil.parse("2003-01-01"));
    Expression vals = Exp.of(v1, v2, v3, v4);

    C2R c = new C2R();
    Expression e =
        Filter.of(vals, Exp.ofConstant("ge"), Exp.of(5), Exp.ofConstant("lt"), Exp.of(15));

    assertEquals(
        List.of(BigDecimal.valueOf(5), BigDecimal.valueOf(10), BigDecimal.valueOf(8)),
        Expressions.getNumberValues(c.calculate(e)));
  }

  @Test
  public void testFilter1b() {
    Value v1 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v2 = Val.of(10, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(15, DateUtil.parse("2002-01-01"));
    Value v4 = Val.of(8, DateUtil.parse("2003-01-01"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype p =
        new Phe("p")
            .expression(
                Filter.of(
                    Exp.of(a), Exp.ofConstant("ge"), Exp.of(5), Exp.ofConstant("lt"), Exp.of(15)))
            .get();

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(
        List.of(BigDecimal.valueOf(5), BigDecimal.valueOf(10), BigDecimal.valueOf(8)),
        Expressions.getNumberValues(c.calculate(p)));
  }

  @Test
  public void testFilter2() {
    Value v1 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v2 = Val.of(10, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(15, DateUtil.parse("2002-01-01"));
    Value v4 = Val.of(8, DateUtil.parse("2003-01-01"));
    Expression vals = Exp.of(v1, v2, v3, v4);

    C2R c = new C2R();
    Expression e =
        Filter.of(
            vals,
            Exp.ofConstant("ge"),
            Exp.of(DateUtil.parse("2001-01-01")),
            Exp.ofConstant("lt"),
            Exp.of(DateUtil.parse("2002-01-02")));

    assertEquals(
        List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(15)),
        Expressions.getNumberValues(c.calculate(e)));
  }

  @Test
  public void testFilter2b() {
    Value v1 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v2 = Val.of(10, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(15, DateUtil.parse("2002-01-01"));
    Value v4 = Val.of(8, DateUtil.parse("2003-01-01"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype p =
        new Phe("p")
            .expression(
                Filter.of(
                    Exp.of(a),
                    Exp.ofConstant("ge"),
                    Exp.of(DateUtil.parse("2001-01-01")),
                    Exp.ofConstant("lt"),
                    Exp.of(DateUtil.parse("2002-01-02"))))
            .get();

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(
        List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(15)),
        Expressions.getNumberValues(c.calculate(p)));
  }

  @Test
  public void testFilter2c() {
    Value v1 = Val.of(DateUtil.parse("2001-01-03"), DateUtil.parse("2000-01-02"));
    Value v2 = Val.of(DateUtil.parse("2000-01-03"), DateUtil.parse("2001-01-02"));
    Value v3 = Val.of(DateUtil.parse("2003-01-03"), DateUtil.parse("2002-01-02"));
    Value v4 = Val.of(DateUtil.parse("2002-01-03"), DateUtil.parse("2003-01-02"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype p =
        new Phe("p")
            .expression(
                Filter.of(
                    Exp.of(a),
                    Exp.ofConstant("ge"),
                    Exp.of(DateUtil.parse("2001-01-01")),
                    Exp.ofConstant("le"),
                    Exp.of(DateUtil.parse("2002-01-04"))))
            .get();

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(
        List.of(DateUtil.parse("2001-01-03"), DateUtil.parse("2002-01-03")),
        Expressions.getDateTimeValues(c.calculate(p)));
  }

  @Test
  public void testFilter3() {
    Value v1 = Val.of(5, DateUtil.parse("2000-01-01"));
    Value v2 = Val.of(10, DateUtil.parse("2001-01-01"));
    Value v3 = Val.of(15, DateUtil.parse("2002-01-01"));
    Value v4 = Val.of(8, DateUtil.parse("2003-01-01"));

    Phenotype a = new Phe("a").get();
    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, v1);
    vals.addValue("a", null, v2);
    vals.addValue("a", null, v3);
    vals.addValue("a", null, v4);

    Phenotype p =
        new Phe("p")
            .expression(
                Filter.of(
                    Exp.of(a),
                    Exp.ofConstant("ge"),
                    Exp.of(DateUtil.parse("2001-01-01")),
                    Exp.ofConstant("lt"),
                    Exp.of(DateUtil.parse("2002-01-02")),
                    Exp.ofConstant("gt"),
                    Exp.of(9),
                    Exp.ofConstant("le"),
                    Exp.of(10)))
            .get();

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(List.of(BigDecimal.valueOf(10)), Expressions.getNumberValues(c.calculate(p)));
  }

  @Test
  public void testNot() {
    Expression v1 = Exp.ofFalse();
    Expression v2 = Exp.ofTrue();

    C2R c = new C2R();
    Expression e = Not.of(v1);
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Not.of(v2);
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));
  }

  @Test
  public void testOr() {
    Expression v1 = Exp.ofFalse();
    Expression v2 = Exp.ofTrue();
    Expression v3 = Exp.ofFalse();

    C2R c = new C2R();
    Expression e = Or.of(v1, v2, v3);
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Or.of(v1, v3);
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));
  }

  @Test
  public void testAnd() {
    Expression v1 = Exp.ofTrue();
    Expression v2 = Exp.ofFalse();
    Expression v3 = Exp.ofTrue();

    C2R c = new C2R();
    Expression e = And.of(v1, v2, v3);
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = And.of(v1, v3);
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));
  }

  @Test
  public void testMinTrue() {
    Expression minTrue = Exp.of(2);
    Expression v1 = Exp.ofTrue();
    Expression v2 = Exp.ofFalse();
    Expression v3 = Exp.ofTrue();

    C2R c = new C2R();
    Expression e = MinTrue.of(minTrue, v1, v2);
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = MinTrue.of(minTrue, v1, v2, v3);
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));
  }

  @Test
  public void testSwitch() {
    Expression s1 = Exp.of(1);
    Expression s2 = Exp.of(2);
    Expression s3 = Exp.of(3);
    Expression d = Exp.of(-1);

    Expression v1 = Exp.of(5);
    Expression v2 = Exp.of(10);

    C2R c = new C2R();

    Expression e1 = Eq.of(v1, v2);
    Expression e2 = Lt.of(v1, v2);
    Expression e3 = Gt.of(v1, v2);

    Expression res = Switch.of(e1, s1, e2, s2, e3, s3);

    assertEquals(new BigDecimal("2"), Expressions.getNumberValue(c.calculate(res)));

    res = Switch.of(e1, s1, e3, s3, d);

    assertEquals(new BigDecimal("-1"), Expressions.getNumberValue(c.calculate(res)));

    Expression res1 = Switch.of(e1, s1, e3, s3);

    Exception exception =
        assertThrows(
            ArithmeticException.class,
            () -> {
              c.calculate(res1);
            });
    assertEquals("No default value defined for the function 'switch'!", exception.getMessage());
  }

  @Test
  public void testComparison() {
    C2R c = new C2R();
    Expression e = Eq.of(Exp.of(3), Exp.of(5));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = Eq.of(Exp.of(3), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Ne.of(Exp.of(5), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Ne.of(Exp.of(3), Exp.of(3));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = Lt.of(Exp.of(3), Exp.of(5));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Lt.of(Exp.of(3), Exp.of(3));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = Le.of(Exp.of(3), Exp.of(5));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Le.of(Exp.of(3), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Le.of(Exp.of(3), Exp.of(2));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = Gt.of(Exp.of(5), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Gt.of(Exp.of(3), Exp.of(3));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));

    e = Ge.of(Exp.of(5), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Ge.of(Exp.of(3), Exp.of(3));
    assertTrue(Expressions.hasValueTrue(c.calculate(e)));

    e = Ge.of(Exp.of(2), Exp.of(3));
    assertFalse(Expressions.hasValueTrue(c.calculate(e)));
  }

  @Test
  public void testAvg() {
    C2R c = new C2R();
    Expression e = Avg.of(Exp.of(3), Exp.of(5), Exp.of(10));

    assertEquals(
        new BigDecimal("6.00"),
        Expressions.getNumberValue(c.calculate(e)).setScale(2, RoundingMode.HALF_UP));

    c = new C2R().defaultAggregateFunction(Avg.get());
    e = Avg.of(Exp.of(2, 3, 4), Exp.of(5), Exp.of(10));

    assertEquals(
        new BigDecimal("6.00"),
        Expressions.getNumberValue(c.calculate(e)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testAvg2() {
    Phenotype p = new Phe("p").expression(Avg.of(Exp.ofEntity("a"), Exp.of(5))).get();
    Phenotype a = new Phe("a").get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, Val.of(12, LocalDateTime.parse("2020-01-02T00:00")));
    vals.addValue("a", null, Val.of(3, LocalDateTime.parse("2021-01-02T00:00")));
    vals.addValue("a", null, Val.of(10, LocalDateTime.parse("2022-01-02T00:00")));
    vals.addValue("a", null, Val.of(7, LocalDateTime.parse("2019-01-02T00:00")));

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Min.get());
    assertEquals(
        new BigDecimal("4.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Max.get());
    assertEquals(
        new BigDecimal("8.50"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(First.get());
    assertEquals(
        new BigDecimal("6.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Last.get());
    assertEquals(
        new BigDecimal("7.50"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testAvg3() {
    Phenotype p = new Phe("p").expression(Avg.of(Exp.ofEntity("a"))).get();
    Phenotype a = new Phe("a").get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, Val.of(12, LocalDateTime.parse("2020-01-02T00:00")));
    vals.addValue("a", null, Val.of(3, LocalDateTime.parse("2021-01-02T00:00")));
    vals.addValue("a", null, Val.of(10, LocalDateTime.parse("2022-01-02T00:00")));
    vals.addValue("a", null, Val.of(7, LocalDateTime.parse("2019-01-02T00:00")));

    Entities phens = Entities.of(p, a);

    C2R c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Min.get());
    assertEquals(
        new BigDecimal("8.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Max.get());
    assertEquals(
        new BigDecimal("8.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(First.get());
    assertEquals(
        new BigDecimal("8.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));

    vals.remove("p");
    c = new C2R().phenotypes(phens).values(vals).defaultAggregateFunction(Last.get());
    assertEquals(
        new BigDecimal("8.00"),
        Expressions.getNumberValue(c.calculate(p)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testMin() {
    C2R c = new C2R();
    Expression e = Min.of(Exp.of(5), Exp.of(3), Exp.of(10));
    assertEquals(
        new BigDecimal("3.00"),
        Expressions.getNumberValue(c.calculate(e)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testMax() {
    C2R c = new C2R();
    Expression e = Max.of(Exp.of(5), Exp.of(13), Exp.of(10));
    assertEquals(
        new BigDecimal("13.00"),
        Expressions.getNumberValue(c.calculate(e)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testBMI() {
    Phenotype bmi =
        new Phe("bmi")
            .expression(Divide.of(Exp.ofEntity("m"), Power.of(Exp.ofEntity("l"), Exp.of(2))))
            .get();
    Phenotype m = new Phe("m").get();
    Phenotype l = new Phe("l").get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("m", null, Val.of(65));
    vals.addValue("l", null, Val.of(1.78));

    Entities phens = Entities.of(bmi, m, l);

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertEquals(
        new BigDecimal("20.52"),
        Expressions.getNumberValue(c.calculate(bmi)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testBMI2() {
    Phenotype bmi =
        new Phe("bmi")
            .expression(Divide.of(Exp.ofEntity("m"), Power.of(Exp.ofEntity("l"), Exp.of(2))))
            .get();
    Phenotype m = new Phe("m").get();
    Phenotype l = new Phe("l").get();
    Phenotype bmiR = new Phe("bmiR").restriction(bmi, Res.gt(20)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("m", null, Val.of(65));
    vals.addValue("l", null, Val.of(1.78));

    Entities phens = Entities.of(bmi, m, l, bmiR).deriveAdditionalProperties();

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertTrue(Expressions.getBooleanValue(c.calculate(bmiR)));
  }

  @Test
  public void testBMI3() {
    Phenotype bmi =
        new Phe("bmi")
            .expression(Divide.of(Exp.ofEntity("m"), Power.of(Exp.ofEntity("l"), Exp.of(2))))
            .get();
    Phenotype m = new Phe("m").get();
    Phenotype l = new Phe("l").get();
    Phenotype bmiR = new Phe("bmiR").restriction(bmi, Res.lt(20)).get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("m", null, Val.of(65));
    vals.addValue("l", null, Val.of(1.78));

    Entities phens = Entities.of(bmi, m, l, bmiR).deriveAdditionalProperties();

    C2R c = new C2R().phenotypes(phens).values(vals);

    assertFalse(Expressions.getBooleanValue(c.calculate(bmiR)));
  }

  @Test
  public void test1() {
    Phenotype p =
        new Phe("p")
            .expression(
                Multiply.of(Exp.ofEntity("a"), Add.of(Exp.ofEntity("b"), Exp.ofEntity("c"))))
            .get();
    Phenotype a = new Phe("a").get();
    Phenotype b = new Phe("b").get();
    Phenotype c = new Phe("c").get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, Val.of(2));
    vals.addValue("b", null, Val.of(3));
    vals.addValue("c", null, Val.of(4));

    Entities phens = Entities.of(p, a, b, c);

    C2R c2r = new C2R().phenotypes(phens).values(vals);

    assertEquals(new BigDecimal("14.00"), Expressions.getNumberValue(c2r.calculate(p)).setScale(2));
  }

  @Test
  public void test2() {
    Phenotype p = new Phe("p").expression(Power.of(Exp.ofEntity("a"), Exp.ofEntity("b"))).get();
    Phenotype a = new Phe("a").get();
    Phenotype b = new Phe("b").get();

    SubjectPhenotypes vals = new SubjectPhenotypes("1");
    vals.addValue("a", null, Val.of(-5));
    vals.addValue("b", null, Val.of(3));

    Entities phens = Entities.of(p, a, b);

    C2R c2r = new C2R().phenotypes(phens).values(vals);

    assertEquals(
        new BigDecimal("-125.00"), Expressions.getNumberValue(c2r.calculate(p)).setScale(2));
  }

  @Test
  public void test3() {
    C2R c = new C2R();
    Expression e = Power.of(Exp.ofConstant("pi"), Exp.ofConstant("e"));

    assertEquals(
        new BigDecimal("22.46"),
        Expressions.getNumberValue(c.calculate(e)).setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testAge() {
    C2R c = new C2R();
    Expression e =
        DiffYears.of(Exp.of(DateUtil.parse("1990-08-10")), Exp.of(DateUtil.parse("2030-06-16")));
    assertEquals(39, Expressions.getNumberValue(c.calculate(e)).intValue());

    e = DiffYears.of(Exp.of(DateUtil.parse("1990-08-10")), Exp.ofConstant("now"));
    assertEquals(
        DateUtil.getPeriodInYears(DateUtil.parse("1990-08-10")).intValue(),
        Expressions.getNumberValue(c.calculate(e)).intValue());
  }

  @Test
  public void testPlusYears() {
    C2R c = new C2R();
    Expression e = PlusYears.of(Exp.of(DateUtil.parse("1990-08-10")), Exp.of(2));
    assertEquals(DateUtil.parse("1992-08-10"), Expressions.getDateTimeValue(c.calculate(e)));
  }

  @Test
  public void testInSet() {
    C2R c = new C2R().defaultAggregateFunction(Avg.get());
    Expression e = In.of(Exp.of(2, 10), Exp.of(5, 6, 7));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R().defaultAggregateFunction(Avg.get());
    e = In.of(Exp.of(2, 10), Exp.of(5, 7));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(1, 2), Exp.of(Res.of(Quantifier.ALL, 1, 2, 3)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(1, 2, 4), Exp.of(Res.of(Quantifier.ALL, 1, 2, 3)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(5, 6, 2), Exp.of(Res.of(1, Quantifier.MIN, 1, 2, 3)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(5, 6, 7), Exp.of(Res.of(1, Quantifier.MIN, 1, 2, 3)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(5, 6, 2), Exp.of(Res.of(2, Quantifier.MIN, 1, 2, 3)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e = In.of(Exp.of(5, 1, 2), Exp.of(Res.of(2, Quantifier.MIN, 1, 2, 3)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));
  }

  @Test
  public void testInInterval() {
    C2R c = new C2R();
    Expression e =
        In.of(
            Exp.of(11, 20),
            Exp.of(
                Res.of(
                    Quantifier.ALL,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e =
        In.of(
            Exp.of(11, 21),
            Exp.of(
                Res.of(
                    Quantifier.ALL,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e =
        In.of(
            Exp.of(11, 21),
            Exp.of(
                Res.of(
                    1,
                    Quantifier.MIN,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e =
        In.of(
            Exp.of(10, 21),
            Exp.of(
                Res.of(
                    1,
                    Quantifier.MIN,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e =
        In.of(
            Exp.of(15, 6, 21),
            Exp.of(
                Res.of(
                    2,
                    Quantifier.MIN,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertFalse(Expressions.getBooleanValue(c.calculate(e)));

    c = new C2R();
    e =
        In.of(
            Exp.of(15, 6, 20),
            Exp.of(
                Res.of(
                    2,
                    Quantifier.MIN,
                    RestrictionOperator.GREATER_THAN,
                    10,
                    RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
                    20)));
    assertTrue(Expressions.getBooleanValue(c.calculate(e)));
  }
}
