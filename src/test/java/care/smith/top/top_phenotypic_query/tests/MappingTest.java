package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import care.smith.top.model.DataType;
import care.smith.top.model.NumberRestriction;
import care.smith.top.model.Quantifier;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringRestriction;
import care.smith.top.top_phenotypic_query.data_adapter.config.CodeMapping;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MappingTest extends AbstractTest {
  DataAdapterConfig config;

  @BeforeEach
  void setup() {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/SQL_Adapter_Test.yml");
    assertNotNull(configFile);

    config = DataAdapterConfig.getInstance(configFile.getPath());
    assertNotNull(config);
  }

  @Test
  public void test1() {
    assertEquals("http://loinc.org|21112-8", config.getBirthdateMapping().getCode());
    assertEquals("http://loinc.org|30525-0", config.getAgeMapping().getCode());
    assertEquals("http://loinc.org|46098-0", config.getSexMapping().getCode());

    Restriction femaleModel =
        new StringRestriction()
            .addValuesItem("http://hl7.org/fhir/administrative-gender|female")
            .type(DataType.STRING);
    Restriction femaleSourceExpected =
        new StringRestriction().addValuesItem("female").type(DataType.STRING);
    Restriction femaleSourceActual = config.getSexMapping().getSourceRestriction(femaleModel, null);
    assertEquals(femaleSourceExpected, femaleSourceActual);

    Restriction ageModel =
        new NumberRestriction()
            .minOperator(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO)
            .maxOperator(RestrictionOperator.LESS_THAN)
            .addValuesItem(BigDecimal.valueOf(18))
            .addValuesItem(BigDecimal.valueOf(34))
            .cardinality(1)
            .quantifier(Quantifier.EXACT)
            .type(DataType.NUMBER);
    Restriction ageSourceExpected =
        new NumberRestriction()
            .minOperator(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO)
            .maxOperator(RestrictionOperator.LESS_THAN)
            .addValuesItem(BigDecimal.valueOf(19))
            .addValuesItem(BigDecimal.valueOf(34))
            .cardinality(1)
            .quantifier(Quantifier.EXACT)
            .type(DataType.NUMBER);
    Restriction ageSourceActual = config.getAgeMapping().getSourceRestriction(ageModel, null);
    assertEquals(ageSourceExpected, ageSourceActual);

    CodeMapping heightMap = config.getCodeMapping("http://loinc.org|3137-7");
    assertEquals("Assessment1", heightMap.getType());
    assertEquals("height", heightMap.getPhenotypeMapping("phenotype"));

    CodeMapping weightMap = config.getCodeMapping("http://loinc.org|3141-9");
    assertEquals("Assessment1", weightMap.getType());
    assertEquals("weight", weightMap.getPhenotypeMapping("phenotype"));
  }

  @Test
  public void test2() {
    Restriction model =
        Res.of(
            Quantifier.ALL,
            RestrictionOperator.GREATER_THAN_OR_EQUAL_TO,
            1.5,
            RestrictionOperator.LESS_THAN,
            2);
    CodeMapping heightMap = config.getCodeMapping("http://loinc.org|3137-7");
    Restriction source = heightMap.getSourceRestriction(model, height);
    assertEquals(
        List.of(BigDecimal.valueOf(150), BigDecimal.valueOf(200)),
        Restrictions.getNumberValues(source));
  }
}
