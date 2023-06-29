package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import com.google.common.collect.ImmutableMap;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigTest {
  DataAdapterConfig conf;

  @BeforeEach
  void setup() {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/SQL_Adapter_Test.yml");
    assertNotNull(configFile);

    conf = DataAdapterConfig.getInstance(configFile.getPath());
    assertNotNull(conf);
  }

  @Test
  public void testSubjectQuery() {
    String expected =
        "SELECT subject_id, birth_date, sex FROM subject\n"
            + "WHERE TRUE\n"
            + "AND sex IN ('m', 'w')\n"
            + "AND birth_date >= '1990-01-01'::date\n"
            + "AND birth_date < '2000-01-01'::date";

    String actual =
        conf.getSubjectQuery()
            .getQueryBuilder()
            .baseQuery()
            .sexList("'m', 'w'")
            .birthdateIntervalLimit(">=", "'1990-01-01'::date")
            .birthdateIntervalLimit("<", "'2000-01-01'::date")
            .build();

    assertEquals(expected, actual);
  }

  @Test
  public void testPhenotypeQuery1() {
    String expected =
        "SELECT subject_id, created_at, weight FROM assessment1\n"
            + "WHERE weight IS NOT NULL\n"
            + "AND weight >= 100\n"
            + "AND weight < 200\n"
            + "AND created_at >= '1990-01-01'::date\n"
            + "AND created_at < '2000-01-01'::date\n"
            + "AND subject_id IN ('1', '2', '3')";

    Map<String, String> mapping = ImmutableMap.of("phenotype", "weight");

    String actual =
        conf.getPhenotypeQuery("Assessment1")
            .getQueryBuilder(mapping)
            .baseQuery()
            .valueIntervalLimit(">=", "100")
            .valueIntervalLimit("<", "200")
            .dateTimeIntervalLimit(">=", "'1990-01-01'::date")
            .dateTimeIntervalLimit("<", "'2000-01-01'::date")
            .subjects("'1', '2', '3'")
            .build();

    assertEquals(expected, actual);
  }

  @Test
  public void testPhenotypeQuery2() {
    String expected =
        "SELECT subject_id, created_at, weight_finding FROM assessment1\n"
            + "WHERE weight_finding IS NOT NULL\n"
            + "AND weight_finding IN ('overweight')\n"
            + "AND created_at >= '1990-01-01'::date\n"
            + "AND created_at < '2000-01-01'::date\n"
            + "AND subject_id IN ('1', '2', '3')";

    Map<String, String> mapping = ImmutableMap.of("phenotype", "weight_finding");

    String actual =
        conf.getPhenotypeQuery("Assessment1")
            .getQueryBuilder(mapping)
            .baseQuery()
            .valueList("'overweight'")
            .dateTimeIntervalLimit(">=", "'1990-01-01'::date")
            .dateTimeIntervalLimit("<", "'2000-01-01'::date")
            .subjects("'1', '2', '3'")
            .build();

    assertEquals(expected, actual);
  }
}
