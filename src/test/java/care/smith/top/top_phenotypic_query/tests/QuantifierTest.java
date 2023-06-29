package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Entity;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Quantifier;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class QuantifierTest {

  private static final String CONFIG = "config/SQL_Adapter_QuantifierTest.yml";

  private static final Phenotype weight =
      new Phe("weight", "http://loinc.org", "3141-9").number("kg").get();

  @Test
  public void testMin1() throws InstantiationException {
    Restriction r = Res.of(1, Quantifier.MIN, RestrictionOperator.GREATER_THAN, 100);
    Phenotype heavy = new Phe("heavy").restriction(weight, r).get();
    Entity[] phenotypes = {weight, heavy};
    ResultSet rs = new Que(CONFIG, phenotypes).inc(heavy).execute();
    assertEquals(Set.of("1", "2", "3", "5"), rs.getSubjectIds());
  }

  @Test
  public void testMin3() throws InstantiationException {
    Restriction r = Res.of(3, Quantifier.MIN, RestrictionOperator.GREATER_THAN, 100);
    Phenotype heavy = new Phe("heavy").restriction(weight, r).get();
    Entity[] phenotypes = {weight, heavy};
    ResultSet rs = new Que(CONFIG, phenotypes).inc(heavy).execute();
    assertEquals(Set.of("2"), rs.getSubjectIds());
  }

  @Test
  public void testMax1() throws InstantiationException {
    Restriction r = Res.of(1, Quantifier.MAX, RestrictionOperator.GREATER_THAN, 100);
    Phenotype heavy = new Phe("heavy").restriction(weight, r).get();
    Entity[] phenotypes = {weight, heavy};
    ResultSet rs = new Que(CONFIG, phenotypes).inc(heavy).execute();
    assertEquals(Set.of("1"), rs.getSubjectIds());
  }

  @Test
  public void testExact2() throws InstantiationException {
    Restriction r = Res.of(2, Quantifier.EXACT, RestrictionOperator.GREATER_THAN, 100);
    Phenotype heavy = new Phe("heavy").restriction(weight, r).get();
    Entity[] phenotypes = {weight, heavy};
    ResultSet rs = new Que(CONFIG, phenotypes).inc(heavy).execute();
    assertEquals(Set.of("3", "5"), rs.getSubjectIds());
  }

  @Test
  public void testAll() throws InstantiationException {
    Restriction r = Res.of(Quantifier.ALL, RestrictionOperator.GREATER_THAN, 100);
    Phenotype heavy = new Phe("heavy").restriction(weight, r).get();
    Entity[] phenotypes = {weight, heavy};
    ResultSet rs = new Que(CONFIG, phenotypes).inc(heavy).execute();
    assertEquals(Set.of("5"), rs.getSubjectIds());
  }
}
