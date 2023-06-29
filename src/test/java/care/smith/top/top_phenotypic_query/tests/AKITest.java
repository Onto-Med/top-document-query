package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Entity;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.If;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Count;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutLast;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Max;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Median;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Min;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Divide;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusDays;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Empty;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Filter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class AKITest {

  private static final String CONFIG = "config/AKI_SQL_Adapter.yml";

  private static Phenotype crea =
      new Phe("crea", "http://loinc.org", "2160-0")
          .itemType(ItemType.OBSERVATION)
          .titleEn("Creatinine [Mass/volume] in Serum or Plasma")
          .number("mmol/L")
          .get();

  private static Phenotype age = new Phe("age", "http://loinc.org", "30525-0").number("a").get();
  private static Phenotype young = new Phe("young").restriction(age, Res.lt(18)).get();
  private static Phenotype old = new Phe("old").restriction(age, Res.ge(18)).get();

  private static Phenotype count =
      new Phe("count").titleEn("Creatinine values count").expression(Count.of(crea)).get();

  private static Phenotype countGt1 =
      new Phe("countGt1")
          .titleEn("Creatinine values count > 1")
          .restriction(count, Res.gt(1))
          .get();

  private static Phenotype refCrea = new Phe("refCrea").expression(CutLast.of(crea)).get();

  private static Phenotype refCrea0_7 =
      new Phe("refCrea0_7")
          .expression(
              Filter.of(
                  Exp.of(refCrea),
                  Exp.ofConstant("ge"),
                  PlusDays.of(Exp.ofConstant("today"), Exp.of(-7)),
                  Exp.ofConstant("lt"),
                  Exp.ofConstant("now")))
          .get();
  private static Phenotype refCrea8_365 =
      new Phe("refCrea8_365")
          .expression(
              Filter.of(
                  Exp.of(refCrea),
                  Exp.ofConstant("ge"),
                  PlusDays.of(Exp.ofConstant("today"), Exp.of(-365)),
                  Exp.ofConstant("lt"),
                  PlusDays.of(Exp.ofConstant("today"), Exp.of(-7))))
          .get();

  private static Phenotype exist0_7 =
      new Phe("exist0_7").expression(Not.of(Empty.of(refCrea0_7))).get();
  private static Phenotype exist8_365 =
      new Phe("exist8_365").expression(Not.of(Empty.of(refCrea8_365))).get();

  private static Phenotype rvRatio0_7 =
      new Phe("rvRatio0_7")
          .expression(
              If.of(Exp.of(exist0_7), Divide.of(Exp.of(crea), Min.of(refCrea0_7)), Exp.of(-1)))
          .get();
  private static Phenotype rvRatio8_365 =
      new Phe("rvRatio8_365")
          .expression(
              If.of(
                  Exp.of(exist8_365), Divide.of(Exp.of(crea), Median.of(refCrea8_365)), Exp.of(-1)))
          .get();

  private static Phenotype rvRatio =
      new Phe("rvRatio").expression(Max.of(rvRatio0_7, rvRatio8_365)).get();
  private static Phenotype rvRatioGe1_5 =
      new Phe("rvRatioGe1_5").restriction(rvRatio, Res.ge(1.5)).get();

  private static Entity[] entities = {
    crea,
    count,
    countGt1,
    refCrea,
    refCrea0_7,
    refCrea8_365,
    exist0_7,
    exist8_365,
    rvRatio0_7,
    rvRatio8_365,
    rvRatio,
    rvRatioGe1_5,
    age,
    young,
    old
  };

  @Test
  public void test() throws InstantiationException {
    DateTimeRestriction dtr =
        Res.geLe(
            LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(365), LocalDateTime.now());
    ResultSet rs =
        new Que(CONFIG, entities)
            .inc(countGt1, dtr)
            .inc(rvRatioGe1_5, dtr)
            .inc(old)
            .executeSql(CREATE_SBJ, CREATE_ASM, insertSubjects(), insertAssesments())
            .execute();

    //    System.out.println(rs);

    assertEquals(Set.of("1", "2", "5"), rs.getSubjectIds());

    assertEquals(BigDecimal.valueOf(2), rs.getNumberValue("1", "rvRatio0_7", dtr));
    assertEquals(BigDecimal.valueOf(-1), rs.getNumberValue("1", "rvRatio8_365", dtr));
    assertEquals(BigDecimal.valueOf(2), rs.getNumberValue("1", "rvRatio", dtr));
    assertTrue(rs.getBooleanValue("1", "rvRatioGe1_5", dtr));

    assertEquals(BigDecimal.valueOf(-1), rs.getNumberValue("2", "rvRatio0_7", dtr));
    assertEquals(BigDecimal.valueOf(3), rs.getNumberValue("2", "rvRatio8_365", dtr));
    assertEquals(BigDecimal.valueOf(3), rs.getNumberValue("2", "rvRatio", dtr));
    assertTrue(rs.getBooleanValue("2", "rvRatioGe1_5", dtr));

    assertEquals(BigDecimal.valueOf(2), rs.getNumberValue("5", "rvRatio0_7", dtr));
    assertEquals(BigDecimal.valueOf(3), rs.getNumberValue("5", "rvRatio8_365", dtr));
    assertEquals(BigDecimal.valueOf(3), rs.getNumberValue("5", "rvRatio", dtr));
    assertTrue(rs.getBooleanValue("5", "rvRatioGe1_5", dtr));
  }

  private static final String LS = System.lineSeparator();
  private static final String CREATE_SBJ =
      "CREATE TABLE subject ("
          + LS
          + "    subject_id bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY,"
          + LS
          + "    birth_date timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,"
          + LS
          + "    sex        text NOT NULL,"
          + LS
          + "    PRIMARY KEY (subject_id)"
          + LS
          + ")";
  private static final String CREATE_ASM =
      "CREATE TABLE assessment1 ("
          + LS
          + "    assessment_id bigint                   NOT NULL GENERATED BY DEFAULT AS IDENTITY,"
          + LS
          + "    subject_id    bigint                   NOT NULL REFERENCES subject,"
          + LS
          + "    created_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,"
          + LS
          + "    crea        numeric,"
          + LS
          + "    PRIMARY KEY (assessment_id)"
          + LS
          + ")";

  private String insertSubjects() {
    LocalDateTime now = LocalDateTime.now();
    StringBuffer sb = new StringBuffer("INSERT INTO subject VALUES").append(LS);
    sb.append(insertSbj(1, now.minusYears(20), "female")).append(",").append(LS);
    sb.append(insertSbj(2, now.minusYears(30), "female")).append(",").append(LS);
    sb.append(insertSbj(3, now.minusYears(40), "male")).append(",").append(LS);
    sb.append(insertSbj(4, now.minusYears(50), "male")).append(",").append(LS);
    sb.append(insertSbj(5, now.minusYears(60), "female"));
    return sb.toString();
  }

  private String insertSbj(Integer id, LocalDateTime bd, String sex) {
    return bracket(String.join(", ", id.toString(), quote(DateUtil.format(bd)), quote(sex)));
  }

  //  (1, '2023-03-12T12:43:00', 180),
  //  (1, '2023-03-11T12:43:00', 100),
  //  (1, '2023-03-10T12:43:00', 90),

  //  (2, '2023-03-12T12:43:00', 180),
  //  (2, '2023-01-02T12:43:00', 110),
  //  (2, '2023-01-03T12:43:00', 65),
  //  (2, '2023-01-04T12:43:00', 55),
  //  (2, '2023-01-05T12:43:00', 50),

  //  (3, '2023-03-12T12:43:00', 180),
  //  (3, '2020-01-02T12:43:00', 110),
  //  (3, '2020-01-03T12:43:00', 105),

  //  (4, '2023-03-12T12:43:00', 190),
  //  (4, '2023-01-02T12:43:00', 170),
  //  (4, '2023-01-03T12:43:00', 160),

  //  (5, '2023-03-12T12:43:00', 180),
  //  (5, '2023-03-11T12:43:00', 100),
  //  (5, '2023-03-10T12:43:00', 90),
  //  (5, '2023-01-02T12:43:00', 110),
  //  (5, '2023-01-03T12:43:00', 65),
  //  (5, '2023-01-04T12:43:00', 55),
  //  (5, '2023-01-05T12:43:00', 50);

  private String insertAssesments() {
    LocalDateTime now = LocalDateTime.now();
    StringBuffer sb =
        new StringBuffer("INSERT INTO assessment1 (subject_id, created_at, crea) VALUES")
            .append(LS);
    sb.append(insertAsm(1, now.minusDays(1), 180)).append(",").append(LS);
    sb.append(insertAsm(1, now.minusDays(2), 100)).append(",").append(LS);
    sb.append(insertAsm(1, now.minusDays(3), 90)).append(",").append(LS);

    sb.append(insertAsm(2, now.minusDays(1), 180)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(2), 110)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(3), 65)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(4), 55)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(5), 50)).append(",").append(LS);

    sb.append(insertAsm(3, now.minusDays(1), 180)).append(",").append(LS);
    sb.append(insertAsm(3, now.minusYears(2), 110)).append(",").append(LS);
    sb.append(insertAsm(3, now.minusYears(2), 105)).append(",").append(LS);

    sb.append(insertAsm(4, now.minusDays(1), 180)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusDays(2), 170)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusDays(3), 169)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(2), 168)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(3), 167)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(4), 166)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(5), 165)).append(",").append(LS);

    sb.append(insertAsm(5, now.minusDays(1), 180)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusDays(2), 100)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusDays(3), 90)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(2), 110)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(3), 65)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(4), 55)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(5), 50));

    return sb.toString();
  }

  private String insertAsm(Integer sbjId, LocalDateTime date, Integer crea) {
    return bracket(
        String.join(", ", sbjId.toString(), quote(DateUtil.format(date)), crea.toString()));
  }

  private String quote(String s) {
    return "'" + s + "'";
  }

  private String bracket(String s) {
    return "(" + s + ")";
  }
}
