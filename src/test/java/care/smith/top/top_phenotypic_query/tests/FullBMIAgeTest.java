package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.model.PhenotypeQuery;
import care.smith.top.model.ProjectionEntry.TypeEnum;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.util.Values;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class FullBMIAgeTest extends AbstractTest {

  @Test
  public void test() throws InstantiationException, SQLException {
    QueryCriterion cri1 =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .defaultAggregationFunctionId(defAgrFunc.getId())
                .subjectId(overWeight.getId())
                .dateTimeRestriction(getDTR(2000))
                .type(TypeEnum.QUERYCRITERION);
    QueryCriterion cri2 =
        (QueryCriterion) new QueryCriterion().inclusion(true).subjectId(female.getId());
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri1).addCriteriaItem(cri2);
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/SQL_Adapter_Test3.yml");
    assertNotNull(configFile);
    DataAdapter adapter = DataAdapter.getInstance(configFile.getPath());

    PhenotypeFinder pf = new PhenotypeFinder(query, phenotypes, adapter);
    ResultSet rs = pf.execute();
    adapter.close();

    assertEquals(Set.of("1"), rs.getSubjectIds());
    assertEquals(13, rs.getPhenotypes("1").size());
    assertTrue(rs.getPhenotypes("1").hasPhenotype(overWeight.getId()));
    assertTrue(rs.getPhenotypes("1").hasPhenotype(female.getId()));

    SubjectPhenotypes phes = rs.getPhenotypes("1");
    Set<String> phesExpected = new HashSet<>(pf.getPhenotypes().getIds());
    phesExpected.add("birthdate");
    phesExpected.remove("BMI27_30");
    phesExpected.remove("BMI19_27");
    phesExpected.remove("Male");
    assertEquals(phesExpected, phes.getPhenotypeNames());

    assertEquals(new BigDecimal(21), Values.getNumberValue(getValue("Age", phes)));
    assertFalse(Values.getBooleanValue(getValue("Old", phes)));
    assertTrue(Values.getBooleanValue(getValue("Young", phes)));

    assertEquals("female", Values.getStringValue(getValue("Sex", phes)));
    assertTrue(Values.getBooleanValue(getValue("Female", phes)));

    assertEquals(new BigDecimal("25.95155709342561"), Values.getNumberValue(getValue("BMI", phes)));
    assertFalse(Values.getBooleanValue(getValue("BMI19_25", phes)));
    assertTrue(Values.getBooleanValue(getValue("BMI25_30", phes)));

    assertEquals(BigDecimal.ONE, Values.getNumberValue(getValue("Finding", phes)));
    assertTrue(Values.getBooleanValue(getValue("Overweight", phes)));
  }
}
