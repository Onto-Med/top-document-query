package care.smith.top.top_phenotypic_query.tests.intern;

import care.smith.top.model.PhenotypeQuery;
import care.smith.top.model.ProjectionEntry.TypeEnum;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRClient;
import care.smith.top.top_phenotypic_query.data_adapter.sql.SQLAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.tests.AbstractTest;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.r4.model.Patient;

public class FullBMIAgeTestIntern extends AbstractTest {

  public static void main(String[] args) throws SQLException {
    long start = new Date().getTime();
    test(getFHIRAdapter());
    long end = new Date().getTime();
    System.out.println("==========================");
    System.out.println("time: " + (end - start));
    System.out.println("==========================");
  }

  private static FHIRAdapter getFHIRAdapter() {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/FHIR_Adapter_Test.yml");
    return new FHIRAdapter(DataAdapterConfig.getInstance(configFile.getPath()));
  }

  private static SQLAdapter getSQLAdapter() throws SQLException {
    return new SQLAdapter(DataAdapterConfig.getInstance("test_files/SQL_Adapter_Test_intern.yml"));
  }

  private static void test(DataAdapter adapter) throws SQLException {
    QueryCriterion cri1 =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .defaultAggregationFunctionId(defAgrFunc.getId())
                .subjectId(overWeight.getId())
                .dateTimeRestriction(getDTR(2000))
                .type(TypeEnum.QUERYCRITERION);
    QueryCriterion cri2 =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .subjectId(female.getId())
                .type(TypeEnum.QUERYCRITERION);
    QueryCriterion cri3 =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .subjectId(old.getId())
                .type(TypeEnum.QUERYCRITERION);
    PhenotypeQuery query =
        new PhenotypeQuery().addCriteriaItem(cri1).addCriteriaItem(cri2).addCriteriaItem(cri3);

    PhenotypeFinder pf = new PhenotypeFinder(query, phenotypes, adapter);
    ResultSet rs = pf.execute();
    adapter.close();

    List<String> actualSbjIds = null;
    if (adapter instanceof FHIRAdapter) actualSbjIds = getDBIds(rs.getSubjectIds());
    else actualSbjIds = new ArrayList<String>(rs.getSubjectIds());

    Collections.sort(
        actualSbjIds,
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
          }
        });

    System.out.println("ACTUAL:");
    System.out.println(actualSbjIds);

    List<String> expectedSbjIds = getExpectedSubjectIds();
    System.out.println("EXPECTED:");
    System.out.println(expectedSbjIds);

    List<String> onlyActualSbjIds = new ArrayList<>(actualSbjIds);
    onlyActualSbjIds.removeAll(expectedSbjIds);
    System.out.println("ONLY ACTUAL:");
    System.out.println(onlyActualSbjIds);

    List<String> onlyExpectedSbjIds = new ArrayList<>(expectedSbjIds);
    onlyExpectedSbjIds.removeAll(actualSbjIds);
    System.out.println("ONLY EXPECTED:");
    System.out.println(onlyExpectedSbjIds);

    //    assertEquals(expectedSbjIds, actualSbjIds);
  }

  private static List<String> getDBIds(Set<String> ids) {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/FHIR_Adapter_Test.yml");
    DataAdapterConfig conf = DataAdapterConfig.getInstance(configFile.getPath());
    FHIRClient client = new FHIRClient(conf);

    List<String> dbIds = new ArrayList<>();

    for (String id : ids)
      dbIds.add(
          ((Patient) client.findResources("http://localhost:8080/baseR4/Patient?_id=" + id).get(0))
              .getIdentifierFirstRep()
              .getValue());

    return dbIds;
  }

  private static List<String> getExpectedSubjectIds() throws SQLException {
    java.sql.ResultSet rs =
        getSQLAdapter()
            .executeQuery(
                "SELECT s.subject_id, birth_date, sex, assessment_id, created_at, height, weight,"
                    + " DATE_PART('year', AGE(CURRENT_DATE, birth_date)) years,"
                    + " (weight/((height/100)^2)) bmi FROM subject s, (select distinct on"
                    + " (subject_id) subject_id, assessment_id, created_at, height, weight from"
                    + " assessment1 where height is not null and weight is not null and created_at"
                    + " >= '2000-01-01'::date and created_at < '2001-01-01'::date order by"
                    + " subject_id, created_at desc) a WHERE s.subject_id = a.subject_id AND sex ="
                    + " 'female' AND DATE_PART('year', AGE(CURRENT_DATE, birth_date)) > 18 AND"
                    + " (weight/((height/100)^2)) >= 27 AND (weight/((height/100)^2)) < 30 ORDER BY"
                    + " s.subject_id");
    //    SQLAdapter.print(rs);

    List<String> ids = new ArrayList<>();
    while (rs.next()) ids.add(rs.getString("subject_id"));

    return ids;
  }

  //  private static void print() throws SQLException {
  //    DataAdapterConfig sqlConfig =
  //        DataAdapterConfig.getInstance("test_files/SQL_Adapter_Test_intern.yml");
  //    SQLAdapter sqlAdapter = new SQLAdapter(sqlConfig);
  //    java.sql.ResultSet rs =
  //        sqlAdapter.executeQuery(
  //            "SELECT s.subject_id, birth_date, sex, assessment_id, created_at, height, weight
  // FROM subject s, assessment1 a WHERE s.subject_id = a.subject_id AND s.subject_id = 3269 ORDER
  // BY created_at");
  //    SQLAdapter.print(rs);
  //  }
  //
  //  public static void testBMI() {
  //    Calculator c = new Calculator();
  //    MathExpression e =
  //        new FunctionExpression("divide")
  //            .arg(new VariableExpression("m"))
  //            .arg(
  //                new FunctionExpression("power")
  //                    .arg(new VariableExpression("l"))
  //                    .arg(new ConstantExpression(new DecimalValue(2))));
  //
  //    c.setVariable("m", 76.6057689700064);
  //    c.setVariable("l", UCUM.convert(new BigDecimal("167.617921540792"), "cm", "m"));
  //
  //    System.out.println(c.calculate(e).getValueDecimal());
  //  }
  //
  //  private static void test() {
  //    Values weightVals1 = new Values("Weight");
  //    weightVals1.setDecimalValues(getDTR(2000), new DecimalValue(76.6057689700064));
  //    Values heightVals1 = new Values("Height");
  //    heightVals1.setDecimalValues(getDTR(2000), new DecimalValue(1.67617921540792));
  //    Values ageVals1 = new Values("Age");
  //    ageVals1.setDecimalValues(getDTR(2000), new DecimalValue(57));
  //
  //    Phenotypes phes1 = new Phenotypes("Subject1");
  //    phes1.setValues(weightVals1, heightVals1, ageVals1);
  //
  //    ResultSet rs = new ResultSet();
  //    rs.setPhenotypes(phes1);
  //
  //    QueryCriterion cri =
  //        new QueryCriterion()
  //            .inclusion(true)
  //            .defaultAggregationFunctionId(defAgrFunc.getId())
  //            .subjectId(overWeight.getId())
  //            .dateTimeRestriction(getDTR(2000));
  //
  //    Map<String, Phenotype> phenotypes =
  //        getPhenotypeMap(
  //            weight,
  //            height,
  //            age,
  //            young,
  //            old,
  //            bmi,
  //            bmi19_25,
  //            bmi19_27,
  //            bmi25_30,
  //            bmi27_30,
  //            finding,
  //            overWeight);
  //
  //    System.out.println(rs);
  //
  //    CompositeSearch search = new CompositeSearch(null, cri, rs, phenotypes);
  //    ResultSet finalRS = search.execute();
  //    System.out.println(finalRS);
  //  }
}
