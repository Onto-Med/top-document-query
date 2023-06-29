package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Entity;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.If;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.Switch;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Count;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutLast;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Max;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Median;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Min;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Divide;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Subtract;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Or;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Gt;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusDays;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Empty;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Filter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class AKIScoreTest {

  private static final String CONFIG = "config/AKI_SQL_Adapter.yml";

  private static Phenotype crea =
      new Phe("crea", "http://loinc.org", "2160-0")
          .itemType(ItemType.OBSERVATION)
          .titleEn("Creatinine [Mass/volume] in Serum or Plasma")
          .number("mmol/L")
          .get();

  private static Phenotype creaMan = new Phe("creaMan").restriction(crea, Res.geLe(68, 133)).get();
  private static Phenotype creaManMin = new Phe("creaManMin").restriction(crea, Res.lt(68)).get();

  private static Phenotype creaWoman =
      new Phe("creaWoman").restriction(crea, Res.geLe(53, 97)).get();
  private static Phenotype creaWomanMin =
      new Phe("creaWomanMin").restriction(crea, Res.lt(53)).get();

  private static Phenotype creaChild =
      new Phe("creaChild").restriction(crea, Res.geLe(35, 124)).get();
  private static Phenotype creaChildMin =
      new Phe("creaChildMin").restriction(crea, Res.lt(35)).get();

  private static Phenotype creaChildGtX3ULRI =
      new Phe("creaChildGtX3ULRI").restriction(crea, Res.gt(372)).get();

  private static Phenotype creaOldGt354 =
      new Phe("creaOldGt354").restriction(crea, Res.gt(354)).get();

  private static Phenotype age = new Phe("age", "http://loinc.org", "30525-0").number("a").get();
  private static Phenotype young = new Phe("young").restriction(age, Res.lt(18)).get();
  private static Phenotype old = new Phe("old").restriction(age, Res.ge(18)).get();

  private static Phenotype sex = new Phe("sex", "http://loinc.org", "46098-0").string().get();
  private static Phenotype female = new Phe("female").restriction(sex, Res.of("female")).get();
  private static Phenotype male = new Phe("male").restriction(sex, Res.of("male")).get();

  private static Phenotype ltRI =
      new Phe("ltRI")
          .expression(
              Or.of(
                  And.of(old, female, creaWomanMin),
                  And.of(old, male, creaManMin),
                  And.of(young, creaChildMin)))
          .get();

  private static Phenotype withinRI =
      new Phe("withinRI")
          .expression(
              Or.of(
                  And.of(old, female, creaWoman),
                  And.of(old, male, creaMan),
                  And.of(young, creaChild)))
          .get();

  private static Phenotype scoreRI =
      new Phe("scoreRI")
          .expression(
              Switch.of(
                  Exp.of(ltRI), Exp.of("low"), Exp.of(withinRI), Exp.of("ok"), Exp.of("high")))
          .get();

  private static Phenotype count = new Phe("count").expression(Count.of(crea)).get();
  private static Phenotype countEq1 = new Phe("countEq1").restriction(count, Res.of(1)).get();
  private static Phenotype countGt0 = new Phe("countGt0").restriction(count, Res.gt(0)).get();

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
  private static Phenotype rvRatioGe1_5Lt2 =
      new Phe("rvRatioGe1_5Lt2").restriction(rvRatio, Res.geLt(1.5, 2)).get();
  private static Phenotype rvRatioGe2Lt3 =
      new Phe("rvRatioGe2Lt3").restriction(rvRatio, Res.geLt(2, 3)).get();
  private static Phenotype rvRatioGe3 = new Phe("rvRatioGe3").restriction(rvRatio, Res.ge(3)).get();

  private static Phenotype refCrea48 =
      new Phe("refCrea48")
          .expression(
              Filter.of(
                  Exp.of(refCrea),
                  Exp.ofConstant("ge"),
                  PlusDays.of(Exp.ofConstant("now"), Exp.of(-2)),
                  Exp.ofConstant("lt"),
                  Exp.ofConstant("now")))
          .get();

  private static Phenotype exist48 =
      new Phe("exist48").expression(Not.of(Empty.of(refCrea48))).get();

  private static Phenotype min48 = new Phe("min48").expression(Min.of(refCrea48)).get();

  private static Phenotype changed48 =
      new Phe("changed48").expression(And.of(Exp.of(exist48), Gt.of(crea, min48))).get();

  private static Phenotype diff48 = new Phe("diff48").expression(Subtract.of(crea, min48)).get();
  private static Phenotype diff48Le26 = new Phe("diff48Le26").restriction(diff48, Res.le(26)).get();

  private static Phenotype scoreRvRatioLt1_5 =
      new Phe("scoreRvRatioLt1_5")
          .expression(
              Switch.of(
                  Not.of(changed48),
                  Exp.of("repeat"),
                  Exp.of(diff48Le26),
                  Exp.of("no alert"),
                  Exp.of("alert 1")))
          .get();

  private static Phenotype scoreRvRatioGe1_5 =
      new Phe("scoreRvRatioGe1_5")
          .expression(
              Switch.of(
                  And.of(young, creaChildGtX3ULRI),
                  Exp.of("alert 3"),
                  And.of(old, creaOldGt354),
                  Exp.of("alert 3"),
                  And.of(Exp.of(young), Not.of(creaChildGtX3ULRI), Exp.of(rvRatioGe3)),
                  Exp.of("alert 3"),
                  And.of(Exp.of(old), Not.of(creaOldGt354), Exp.of(rvRatioGe3)),
                  Exp.of("alert 3"),
                  And.of(Exp.of(young), Not.of(creaChildGtX3ULRI), Exp.of(rvRatioGe2Lt3)),
                  Exp.of("alert 2"),
                  And.of(Exp.of(old), Not.of(creaOldGt354), Exp.of(rvRatioGe2Lt3)),
                  Exp.of("alert 2"),
                  And.of(Exp.of(young), Not.of(creaChildGtX3ULRI), Exp.of(rvRatioGe1_5Lt2)),
                  Exp.of("alert 1"),
                  And.of(Exp.of(old), Not.of(creaOldGt354), Exp.of(rvRatioGe1_5Lt2)),
                  Exp.of("alert 1")))
          .get();

  private static Phenotype scoreAKI =
      new Phe("scoreAKI")
          .expression(
              Switch.of(countEq1, scoreRI, rvRatioGe1_5, scoreRvRatioGe1_5, scoreRvRatioLt1_5))
          .get();

  private static Entity[] entities = {
    crea,
    creaMan,
    creaManMin,
    creaWoman,
    creaWomanMin,
    creaChild,
    creaChildMin,
    creaChildGtX3ULRI,
    creaOldGt354,
    age,
    young,
    old,
    sex,
    male,
    female,
    ltRI,
    withinRI,
    scoreRI,
    count,
    countEq1,
    countGt0,
    refCrea,
    refCrea0_7,
    refCrea8_365,
    exist0_7,
    exist8_365,
    rvRatio0_7,
    rvRatio8_365,
    rvRatio,
    rvRatioGe1_5,
    rvRatioGe1_5Lt2,
    rvRatioGe2Lt3,
    rvRatioGe3,
    refCrea48,
    exist48,
    min48,
    changed48,
    diff48,
    diff48Le26,
    scoreRvRatioLt1_5,
    scoreRvRatioGe1_5,
    scoreAKI
  };

  @Test
  public void test() throws InstantiationException {
    DateTimeRestriction dtr =
        Res.geLe(
            LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(365), LocalDateTime.now());
    ResultSet rs =
        new Que(CONFIG, entities)
            .pro(scoreAKI, dtr)
            .pro(age)
            .pro(sex)
            .pro(crea)
            .executeSql(
                DROP_SBJ, DROP_ASM, CREATE_SBJ, CREATE_ASM, insertSubjects(), insertAssesments())
            .execute();

    System.out.println(rs);

    assertEquals(Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), rs.getSubjectIds());

    assertEquals("alert 2", rs.getStringValue("1", "scoreAKI", dtr));
    assertEquals("alert 3", rs.getStringValue("2", "scoreAKI", dtr));
    assertEquals("high", rs.getStringValue("3", "scoreAKI", dtr));
    assertEquals("repeat", rs.getStringValue("4", "scoreAKI", dtr));
    assertEquals("alert 3", rs.getStringValue("5", "scoreAKI", dtr));
    assertEquals("no alert", rs.getStringValue("6", "scoreAKI", dtr));
    assertEquals("alert 1", rs.getStringValue("7", "scoreAKI", dtr));
    assertEquals("alert 3", rs.getStringValue("8", "scoreAKI", dtr));
    assertEquals("alert 3", rs.getStringValue("9", "scoreAKI", dtr));
    assertEquals("alert 2", rs.getStringValue("10", "scoreAKI", dtr));
  }

  private static final String LS = System.lineSeparator();

  private static final String DROP_SBJ = "DROP TABLE IF EXISTS subject";
  private static final String DROP_ASM = "DROP TABLE IF EXISTS assessment1";

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
    sb.append(insertSbj(5, now.minusYears(60), "female")).append(",").append(LS);
    sb.append(insertSbj(6, now.minusYears(70), "male")).append(",").append(LS);
    sb.append(insertSbj(7, now.minusYears(80), "female")).append(",").append(LS);
    sb.append(insertSbj(8, now.minusYears(90), "male")).append(",").append(LS);
    sb.append(insertSbj(9, now.minusYears(16), "female")).append(",").append(LS);
    sb.append(insertSbj(10, now.minusYears(17), "male")).append(LS);
    return sb.toString();
  }

  private String insertSbj(Integer id, LocalDateTime bd, String sex) {
    return bracket(String.join(", ", id.toString(), quote(DateUtil.format(bd)), quote(sex)));
  }

  private String insertAssesments() {
    LocalDateTime now = LocalDateTime.now();
    StringBuffer sb =
        new StringBuffer("INSERT INTO assessment1 (subject_id, created_at, crea) VALUES")
            .append(LS);
    sb.append(insertAsm(1, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(1, now.minusDays(2), 100)).append(",").append(LS);
    sb.append(insertAsm(1, now.minusDays(3), 90)).append(",").append(LS);

    sb.append(insertAsm(2, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(2), 110)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(3), 65)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(4), 55)).append(",").append(LS);
    sb.append(insertAsm(2, now.minusMonths(5), 50)).append(",").append(LS);

    sb.append(insertAsm(3, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(3, now.minusYears(2), 110)).append(",").append(LS);
    sb.append(insertAsm(3, now.minusYears(2), 105)).append(",").append(LS);

    sb.append(insertAsm(4, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusDays(2), 170)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusDays(3), 169)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(2), 168)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(3), 167)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(4), 166)).append(",").append(LS);
    sb.append(insertAsm(4, now.minusMonths(5), 165)).append(",").append(LS);

    sb.append(insertAsm(5, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusDays(2), 100)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusDays(3), 90)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(2), 110)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(3), 65)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(4), 55)).append(",").append(LS);
    sb.append(insertAsm(5, now.minusMonths(5), 50)).append(",");

    sb.append(insertAsm(6, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(6, now.minusDays(1), 170)).append(",").append(LS);
    sb.append(insertAsm(6, now.minusDays(3), 169)).append(",").append(LS);

    sb.append(insertAsm(7, now.minusHours(1), 180)).append(",").append(LS);
    sb.append(insertAsm(7, now.minusDays(1), 150)).append(",").append(LS);
    sb.append(insertAsm(7, now.minusDays(3), 169)).append(",").append(LS);

    sb.append(insertAsm(8, now.minusHours(1), 355)).append(",").append(LS);
    sb.append(insertAsm(8, now.minusDays(2), 150)).append(",").append(LS);
    sb.append(insertAsm(8, now.minusDays(3), 169)).append(",").append(LS);

    sb.append(insertAsm(9, now.minusHours(1), 373)).append(",").append(LS);
    sb.append(insertAsm(9, now.minusDays(2), 150)).append(",").append(LS);
    sb.append(insertAsm(9, now.minusDays(3), 169)).append(",").append(LS);

    sb.append(insertAsm(10, now.minusHours(1), 372)).append(",").append(LS);
    sb.append(insertAsm(10, now.minusDays(2), 150)).append(",").append(LS);
    sb.append(insertAsm(10, now.minusDays(3), 169)).append(LS);

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
