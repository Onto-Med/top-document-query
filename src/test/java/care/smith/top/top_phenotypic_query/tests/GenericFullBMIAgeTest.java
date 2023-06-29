package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class GenericFullBMIAgeTest extends AbstractTest {

  @Test
  public void test1() throws InstantiationException {
    ResultSet rs =
        new Que("config/Generic_SQL_Adapter_Test.yml", phenotypes)
            .inc(overWeight, getDTR(2000), defAgrFunc.getId())
            .inc(female)
            .execute();

    System.out.println(rs);

    assertEquals(Set.of("1"), rs.getSubjectIds());
    assertEquals(13, rs.getPhenotypes("1").size());
    assertTrue(rs.getPhenotypes("1").hasPhenotype(overWeight.getId()));
    assertTrue(rs.getPhenotypes("1").hasPhenotype(female.getId()));

    SubjectPhenotypes phes = rs.getPhenotypes("1");
    Set<String> phesExpected = Entities.of(phenotypes).getPhenotypeIds();
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

  @Test
  public void test2() throws InstantiationException {
    ResultSet rs =
        new Que("config/Generic_SQL_Adapter_Test.yml", phenotypes)
            .inc(overWeight, getDTR(2000), defAgrFunc.getId())
            .exc(male)
            .execute();

    System.out.println(rs);

    assertEquals(Set.of("1"), rs.getSubjectIds());
    assertEquals(13, rs.getPhenotypes("1").size());
    assertTrue(rs.getPhenotypes("1").hasPhenotype(overWeight.getId()));

    SubjectPhenotypes phes = rs.getPhenotypes("1");
    Set<String> phesExpected = Entities.of(phenotypes).getPhenotypeIds();
    phesExpected.add("birthdate");
    phesExpected.remove("BMI27_30");
    phesExpected.remove("BMI19_27");
    phesExpected.remove("Female");
    assertEquals(phesExpected, phes.getPhenotypeNames());

    assertEquals(new BigDecimal(21), Values.getNumberValue(getValue("Age", phes)));
    assertFalse(Values.getBooleanValue(getValue("Old", phes)));
    assertTrue(Values.getBooleanValue(getValue("Young", phes)));

    assertEquals("female", Values.getStringValue(getValue("Sex", phes)));

    assertEquals(new BigDecimal("25.95155709342561"), Values.getNumberValue(getValue("BMI", phes)));
    assertFalse(Values.getBooleanValue(getValue("BMI19_25", phes)));
    assertTrue(Values.getBooleanValue(getValue("BMI25_30", phes)));

    assertEquals(BigDecimal.ONE, Values.getNumberValue(getValue("Finding", phes)));
    assertTrue(Values.getBooleanValue(getValue("Overweight", phes)));
  }
}
