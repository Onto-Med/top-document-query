package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Entity;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.MinTrue;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Or;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRClient;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.util.HashSet;
import java.util.Set;
import org.hl7.fhir.r4.model.Patient;

public class DelirTest {

  private static Phenotype expIcd =
      new Phe("expIcd", "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "F05.-", "F05.0", "F05.1")
          .itemType(ItemType.CONDITION)
          .titleDe("Expliziter ICD-10-Code")
          .titleEn("Explicit ICD-10 code")
          .bool()
          .get();
  private static Phenotype impIcd =
      new Phe("impIcd", "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "A81.2", "A51.2", "G92.-")
          .itemType(ItemType.CONDITION)
          .titleDe("Impliziter ICD-10-Code nach Kim et al.")
          .titleEn("Implicit ICD-10 code by to Kim et al.")
          .bool()
          .get();
  private static Phenotype infect =
      new Phe("infect", "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "G00.0", "G00.1", "G00.2")
          .itemType(ItemType.CONDITION)
          .titleDe("Infektion")
          .titleEn("Infection")
          .bool()
          .get();
  private static Phenotype cognit =
      new Phe("cognit", "http://fhir.de/CodeSystem/bfarm/icd-10-gm", "F00.-", "F01.-", "F02.-")
          .itemType(ItemType.CONDITION)
          .titleDe("Kognitive Beeinträchtigung")
          .titleEn("Cognitive impairment")
          .bool()
          .get();
  private static Phenotype age =
      new Phe("age", "http://loinc.org", "30525-0")
          .itemType(ItemType.OBSERVATION)
          .titleDe("Alter")
          .titleEn("Age")
          .number("a")
          .get();
  private static Phenotype above80 =
      new Phe("above80")
          .titleDe("Über 80 Jahre")
          .titleEn("Above 80 years")
          .restriction(age, Res.ge(80))
          .get();
  private static Phenotype antiPsy =
      new Phe("antiPsy", "http://fhir.de/CodeSystem/bfarm/atc", "N05A")
          .itemType(ItemType.MEDICATION_ADMINISTRATION)
          .titleDe("Antipsychotika Medikation")
          .titleEn("Anti-psychotic medication")
          .bool()
          .get();
  private static Phenotype sodium =
      new Phe("sodium", "http://loinc.org", "2951-2", "2947-0", "32717-1")
          .itemType(ItemType.OBSERVATION)
          .titleDe("Natriumspiegel")
          .titleEn("Sodium level")
          .number("mmol/L")
          .get();
  private static Phenotype lt135 =
      new Phe("lt135")
          .titleDe("< 135 mg/dL")
          .titleEn("< 135 mg/dL")
          .restriction(sodium, Res.lt(58.73))
          .get();
  private static Phenotype gt145 =
      new Phe("gt145")
          .titleDe("> 145 mg/dL")
          .titleEn("> 145 mg/dL")
          .restriction(sodium, Res.gt(63.08))
          .get();
  private static Phenotype op =
      new Phe("op", "http://fhir.de/CodeSystem/bfarm/ops", "5-")
          .itemType(ItemType.PROCEDURE)
          .titleDe("Operation")
          .titleEn("Operation")
          .bool()
          .get();
  private static Phenotype fact2of3 =
      new Phe("fact2of3")
          .titleDe("2/3 Faktoren")
          .titleEn("2/3 factors")
          .descriptionDe(
              "Dies ist ein Hilfsphänotyp zur Überprüfung, ob mindestens 2 von 3 Kriterien erfüllt"
                  + " sind.")
          .descriptionEn(
              "This is a helper phenotype to check whether at least 2 of 3 criteria are fulfilled.")
          .expression(
              MinTrue.of(
                  Exp.of(2), Exp.of(above80), Exp.of(infect), Or.of(Exp.of(lt135), Exp.of(gt145))))
          .get();
  private static Phenotype algA =
      new Phe("algA")
          .titleDe("Basisalgorithmus A")
          .titleEn("Basic algorithm A")
          .synonymDe("Delir Basisalgorithmus A")
          .synonymEn("Delirium basic algorithm A")
          .descriptionDe(
              "Algorithmus zur Identifikation von Delir. Der Algorithmus basiert ausschließlich auf"
                  + " ICD-10-Codes. Es ist zu beachten, dass die Sensitivität gemäß Literatur nur"
                  + " zwischen 18% bis 30% liegt - bei guter Spezifität.")
          .descriptionEn(
              "Algorithm for the identification of delirium. The algorithm is based exclusively on"
                  + " ICD-10 codes. It should be noted that the sensitivity according to literature"
                  + " is only between 18% to 30% - with good specificity.")
          .expression(Exp.of(expIcd))
          .get();
  private static Phenotype algB =
      new Phe("algB")
          .titleDe("Basisalgorithmus B")
          .titleEn("Basic algorithm B")
          .synonymDe("Delir Basisalgorithmus B")
          .synonymEn("Delirium basic algorithm B")
          .descriptionDe(
              "Delir Algorithmus ausschließlich basierend auf ICD-10-Codes, wobei explizite und"
                  + " implizite ICD-10-Codes nach Kim et al. zum Einsatz kommen. Zu beachten ist,"
                  + " dass die Sensitivität gemäß Literatur nur zwischen 18% bis 30% liegt - bei"
                  + " guter Spezifität.")
          .descriptionEn(
              "Delirium algorithm based solely on ICD-10 codes, using explicit and implicit ICD-10"
                  + " codes according to Kim et al. It should be noted that the sensitivity"
                  + " according to the literature is only between 18% and 30% - with good"
                  + " specificity.")
          .expression(Or.of(Exp.of(expIcd), Exp.of(impIcd)))
          .get();
  private static Phenotype extAlg =
      new Phe("extAlg")
          .titleDe("Erweiterter Algorithmus")
          .titleEn("Extended algorithm")
          .synonymDe("Delir erweiterter Algorithmus")
          .synonymEn("Delirium extended algorithm")
          .descriptionDe(
              "Komplexer Delir Algorithmus, der verschiedenste Datenkategorien beinhaltet. Durch"
                  + " Hinzunahme weiterer ICD-10-Codes für kognitive Beeinträchtigung, sowie"
                  + " Einbezug weiterer NICE-Risiko-Score Datenpunkte, soll die Sensitivität erhöht"
                  + " werden.")
          .descriptionEn(
              "Complex delirium algorithm that includes a wide range of data categories. The"
                  + " addition of further ICD-10 codes for cognitive impairment, as well as the"
                  + " inclusion of further NICE risk score data points, should increase the"
                  + " sensitivity.")
          .expression(
              Or.of(
                  Exp.of(expIcd),
                  And.of(Exp.of(antiPsy), Exp.of(op)),
                  And.of(Exp.of(impIcd), Or.of(Exp.of(op), Exp.of(antiPsy))),
                  And.of(Exp.of(cognit), Exp.of(fact2of3))))
          .get();

  private static Phenotype antiPsyAndOp =
      new Phe("antiPsyAndOp").expression(And.of(Exp.of(antiPsy), Exp.of(op))).get();
  private static Phenotype impIcdAndAntiPsyOrOp =
      new Phe("impIcdAndAntiPsyOrOp")
          .expression(And.of(Exp.of(impIcd), Or.of(Exp.of(op), Exp.of(antiPsy))))
          .get();
  private static Phenotype cognitAnd2of3 =
      new Phe("cognitAnd2of3").expression(And.of(Exp.of(cognit), Exp.of(fact2of3))).get();
  private static Phenotype notExtAlg =
      new Phe("notExtAlg")
          .expression(
              Not.of(
                  Or.of(
                      Exp.of(expIcd),
                      And.of(Exp.of(antiPsy), Exp.of(op)),
                      And.of(Exp.of(impIcd), Or.of(Exp.of(op), Exp.of(antiPsy))),
                      And.of(Exp.of(cognit), Exp.of(fact2of3)))))
          .get();
  private static Phenotype sex = new Phe("sex", "http://loinc.org", "46098-0").string().get();
  private static Phenotype female = new Phe("female").restriction(sex, Res.of("female")).get();

  private static Entity[] entities = {
    expIcd,
    impIcd,
    infect,
    cognit,
    age,
    above80,
    antiPsy,
    sodium,
    lt135,
    gt145,
    op,
    fact2of3,
    algA,
    algB,
    extAlg,
    antiPsyAndOp,
    impIcdAndAntiPsyOrOp,
    cognitAnd2of3,
    notExtAlg,
    sex,
    female
  };

  private static void test(Que que, boolean fhir, String... expectedIds) {
    ResultSet rs = que.execute();
    Set<String> actualSbjIds = (fhir) ? getDBIds(que, rs.getSubjectIds()) : rs.getSubjectIds();
    assertEquals(Set.of(expectedIds), actualSbjIds);
  }

  public static void testAlgA(String configFilePath, boolean fhir) throws InstantiationException {
    test(new Que(configFilePath, entities).inc(algA), fhir, "1", "5", "8");
  }

  public static void testExpIcd(String configFilePath, boolean fhir) throws InstantiationException {
    test(new Que(configFilePath, entities).inc(expIcd), fhir, "1", "5", "8");
  }

  public static void testAlgB(String configFilePath, boolean fhir) throws InstantiationException {
    test(new Que(configFilePath, entities).inc(algB), fhir, "1", "2", "5", "6", "8", "10", "11");
  }

  public static void test2of3(String configFilePath, boolean fhir) throws InstantiationException {
    test(new Que(configFilePath, entities).inc(fact2of3), fhir, "2", "3", "4", "10");
  }

  public static void testExtAlg(String configFilePath, boolean fhir) throws InstantiationException {
    test(new Que(configFilePath, entities).inc(extAlg), fhir, "1", "2", "3", "4", "5", "8", "11");
  }

  public static void testExtAlgWithDate(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(
        new Que(configFilePath, entities).inc(extAlg, Res.ge(DateUtil.parse("2000-06-01"))),
        fhir,
        "2",
        "4");
  }

  public static void testNotExtAlg(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(notExtAlg), fhir, "6", "7", "9", "10");
  }

  public static void testExtAlgExcAlgB(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(extAlg).exc(algB), fhir, "3", "4");
  }

  public static void testExtAlgAndFemale(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(extAlg).inc(female), fhir, "1", "2", "5", "11");
  }

  public static void testExtAlgExcFemale(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(extAlg).exc(female), fhir, "3", "4", "8");
  }

  public static void testAntiPsyAndOp(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(antiPsyAndOp), fhir, "1", "3", "5");
  }

  public static void testImpIcdAndAntiPsyOrOp(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(impIcdAndAntiPsyOrOp), fhir, "2", "11");
  }

  public static void testCognitAnd2of3(String configFilePath, boolean fhir)
      throws InstantiationException {
    test(new Que(configFilePath, entities).inc(cognitAnd2of3), fhir, "4");
  }

  private static Set<String> getDBIds(Que que, Set<String> ids) {
    FHIRClient client = new FHIRClient(que.getConfig());
    Set<String> dbIds = new HashSet<>();

    for (String id : ids)
      dbIds.add(
          ((Patient) client.findResources("http://localhost:8080/baseR4/Patient?_id=" + id).get(0))
              .getIdentifierFirstRep()
              .getValue());

    return dbIds;
  }
}
