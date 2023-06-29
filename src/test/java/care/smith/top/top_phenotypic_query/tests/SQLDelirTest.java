package care.smith.top.top_phenotypic_query.tests;

import org.junit.jupiter.api.Test;

public class SQLDelirTest extends DelirTest {

  private static final String CONFIG = "config/Delir_SQL_Adapter_Test.yml";

  @Test
  public void testSQLAlgA() throws InstantiationException {
    testAlgA(CONFIG, false);
  }

  @Test
  public void testSQLExpIcd() throws InstantiationException {
    testExpIcd(CONFIG, false);
  }

  @Test
  public void testSQLAlgB() throws InstantiationException {
    testAlgB(CONFIG, false);
  }

  @Test
  public void testSQL2of3() throws InstantiationException {
    test2of3(CONFIG, false);
  }

  @Test
  public void testSQLExtAlg() throws InstantiationException {
    testExtAlg(CONFIG, false);
  }

  @Test
  public void testSQLExtAlgWithDate() throws InstantiationException {
    testExtAlgWithDate(CONFIG, false);
  }

  @Test
  public void testSQLNotExtAlg() throws InstantiationException {
    testNotExtAlg(CONFIG, false);
  }

  @Test
  public void testSQLExtAlgExcAlgB() throws InstantiationException {
    testExtAlgExcAlgB(CONFIG, false);
  }

  @Test
  public void testSQLExtAlgAndFemale() throws InstantiationException {
    testExtAlgAndFemale(CONFIG, false);
  }

  @Test
  public void testSQLExtAlgExcFemale() throws InstantiationException {
    testExtAlgExcFemale(CONFIG, false);
  }

  @Test
  public void testSQLAntiPsyAndOp() throws InstantiationException {
    testAntiPsyAndOp(CONFIG, false);
  }

  @Test
  public void testSQLImpIcdAndAntiPsyOrOp() throws InstantiationException {
    testImpIcdAndAntiPsyOrOp(CONFIG, false);
  }

  @Test
  public void testSQLCognitAnd2of3() throws InstantiationException {
    testCognitAnd2of3(CONFIG, false);
  }
}
