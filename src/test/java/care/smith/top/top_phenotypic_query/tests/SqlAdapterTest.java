package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import care.smith.top.model.DataType;
import care.smith.top.model.Entity;
import care.smith.top.model.Phenotype;
import care.smith.top.model.PhenotypeQuery;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.sql.SQLAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.util.Entities;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlAdapterTest extends AbstractTest {
  static final String DB_URL = "jdbc:h2:mem:test0-db;INIT=RUNSCRIPT FROM 'classpath:schema.sql'";
  static final String DB_USER = "user";
  static final String DB_PASS = "password";

  Connection con;
  Entities phenotypes;

  @BeforeEach
  void setup() throws SQLException {
    con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    assertNotNull(con);
    assertTrue(con.isValid(0));

    Statement stmt = con.createStatement();
    java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM subject");

    assertTrue(rs.next());
    assertEquals(3, rs.getInt(1));

    Phenotype height = getPhenotype("height", "http://loinc.org", "3137-7");
    Phenotype tall = getIntervalMin("tall", height, 200);

    phenotypes = Entities.of(height, tall);
  }

  @AfterEach
  void cleanup() throws SQLException {
    con.close();
    assertTrue(con.isClosed());
  }

  @Test
  void testAdapterConnection() throws InstantiationException, SQLException {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/SQL_Adapter_Test.yml");
    assertNotNull(configFile);

    DataAdapter adapter = DataAdapter.getInstance(configFile.getPath());
    assertNotNull(adapter);

    Phenotype tall = phenotypes.getPhenotype("tall");
    assertNotNull(tall);

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .defaultAggregationFunctionId("last")
                .subjectId(tall.getId());

    SingleSearch search = new SingleSearch(null, cri, tall, adapter);
    assertNotNull(search);

    ResultSet rs = search.execute();
    assertNotNull(rs);
    assertEquals(1, rs.getSubjectIds().size());
  }

  @Test
  void testCorruptedPhenotype() throws InstantiationException, SQLException {
    DataAdapter adapter = DataAdapter.getInstanceFromResource("config/SQL_Adapter_Test1.yml");

    Phenotype stringPhenotype = getPhenotype("Sex", "http://loinc.org", "46098-0", DataType.STRING);

    Phenotype corrupted =
        getRestriction(
            "corrupted",
            stringPhenotype,
            "'); TRUNCATE TABLE assessment1; SELECT NULL FROM assessment1 WHERE '' IN ('");

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .defaultAggregationFunctionId("last")
                .subjectId(corrupted.getId());

    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri);

    try (java.sql.ResultSet rs =
        ((SQLAdapter) adapter).executeQuery("SELECT count(*) FROM assessment1")) {
      rs.next();
      assertEquals(4, rs.getInt(1));
    }

    PhenotypeFinder finder =
        new PhenotypeFinder(query, new Entity[] {stringPhenotype, corrupted}, adapter);
    finder.execute();

    try (java.sql.ResultSet rs =
        ((SQLAdapter) adapter).executeQuery("SELECT count(*) FROM assessment1")) {
      rs.next();
      assertEquals(4, rs.getInt(1));
    }
  }
}
