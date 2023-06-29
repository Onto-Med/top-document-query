package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import care.smith.top.model.*;
import care.smith.top.model.ProjectionEntry.TypeEnum;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import com.google.common.collect.ObjectArrays;
import java.net.URL;
import java.sql.SQLException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BooleanSinglePhenotypeTest extends AbstractTest {

  private DataAdapter adapter;
  private Phenotype w =
      getPhenotype("Weight", "http://loinc.org", "3141-9").dataType(DataType.BOOLEAN);
  private Phenotype h =
      getPhenotype("Height", "http://loinc.org", "3137-7", "m").dataType(DataType.BOOLEAN);
  private Entity[] phenotypes = {w, h};

  @BeforeEach
  void start() throws InstantiationException {
    URL configFile =
        BooleanSinglePhenotypeTest.class
            .getClassLoader()
            .getResource("config/SQL_Adapter_Test5.yml");
    assertNotNull(configFile);
    adapter = DataAdapter.getInstance(configFile.getPath());
  }

  @AfterEach
  void end() {
    adapter.close();
  }

  @Test
  public void test1() throws SQLException {
    QueryCriterion cri1 =
        (QueryCriterion) new QueryCriterion().inclusion(false).subjectId(h.getId());
    QueryCriterion cri2 =
        (QueryCriterion) new QueryCriterion().inclusion(true).subjectId(w.getId());
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri1).addCriteriaItem(cri2);

    PhenotypeFinder pf = new PhenotypeFinder(query, phenotypes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("2", "4"), rs.getSubjectIds());
  }

  @Test
  public void test2() throws SQLException {
    QueryCriterion cri1 =
        (QueryCriterion) new QueryCriterion().inclusion(true).subjectId(h.getId());
    QueryCriterion cri2 =
        (QueryCriterion) new QueryCriterion().inclusion(false).subjectId(w.getId());
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri1).addCriteriaItem(cri2);

    PhenotypeFinder pf = new PhenotypeFinder(query, phenotypes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("1"), rs.getSubjectIds());
  }

  @Test
  public void test3() throws SQLException {
    Expression exp = And.of(Exp.ofEntity("Weight"), Not.of(Exp.ofEntity("Height")));
    Phenotype composite =
        (Phenotype)
            new Phenotype().expression(exp).id("c").entityType(EntityType.COMPOSITE_PHENOTYPE);

    Entity[] phes = ObjectArrays.concat(phenotypes, new Entity[] {composite}, Entity.class);

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .subjectId(composite.getId())
                .type(TypeEnum.QUERYCRITERION);
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri);

    PhenotypeFinder pf = new PhenotypeFinder(query, phes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("2", "4"), rs.getSubjectIds());
  }

  @Test
  public void test4() throws SQLException {
    Expression exp = And.of(Exp.ofEntity("Height"), Not.of(Exp.ofEntity("Weight")));
    Phenotype composite =
        (Phenotype)
            new Phenotype().expression(exp).id("c").entityType(EntityType.COMPOSITE_PHENOTYPE);

    Entity[] phes = ObjectArrays.concat(phenotypes, new Entity[] {composite}, Entity.class);

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .subjectId(composite.getId())
                .type(TypeEnum.QUERYCRITERION);
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri);

    PhenotypeFinder pf = new PhenotypeFinder(query, phes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("1"), rs.getSubjectIds());
  }

  @Test
  public void test5() throws SQLException {
    Expression exp = And.of(Exp.ofEntity("Weight"), Not.of(Exp.ofEntity("Height")));
    Phenotype composite =
        (Phenotype)
            new Phenotype().expression(exp).id("c").entityType(EntityType.COMPOSITE_PHENOTYPE);

    Entity[] phes = ObjectArrays.concat(phenotypes, new Entity[] {composite}, Entity.class);

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(false)
                .subjectId(composite.getId())
                .type(TypeEnum.QUERYCRITERION);
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri);

    PhenotypeFinder pf = new PhenotypeFinder(query, phes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("1", "3", "5"), rs.getSubjectIds());
  }

  @Test
  public void test6() throws SQLException {
    Expression exp = And.of(Exp.ofEntity("Height"), Not.of(Exp.ofEntity("Weight")));
    Phenotype composite =
        (Phenotype)
            new Phenotype().expression(exp).id("c").entityType(EntityType.COMPOSITE_PHENOTYPE);

    Entity[] phes = ObjectArrays.concat(phenotypes, new Entity[] {composite}, Entity.class);

    QueryCriterion cri =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(false)
                .subjectId(composite.getId())
                .type(TypeEnum.QUERYCRITERION);
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri);

    PhenotypeFinder pf = new PhenotypeFinder(query, phes, adapter);
    ResultSet rs = pf.execute();

    assertEquals(Set.of("2", "3", "4", "5"), rs.getSubjectIds());
  }
}
