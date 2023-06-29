package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Entity;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRAdapter;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class RestrictionTest {

  @Test
  public void test() {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/FHIR_Adapter_Test.yml");
    DataAdapter adapter = new FHIRAdapter(DataAdapterConfig.getInstance(configFile.getPath()));

    Phenotype p = new Phe("p", "http://phe.org", "p").string().get();
    Phenotype p1 = new Phe("p1", "http://phe.org", "p1a", "p1b").restriction(p, null).get();
    Entity[] phenotypes = {p, p1};

    PhenotypeFinder pf = new PhenotypeFinder(null, phenotypes, adapter);

    Phenotype actual = pf.getPhenotypes().getPhenotype("p1");

    Restriction r = Res.of("http://phe.org|p1a", "http://phe.org|p1b");
    Phenotype expected =
        (Phenotype)
            new Phe("p1", "http://phe.org", "p1a", "p1b")
                .restriction(p, r)
                .get()
                .expression(Exp.inRestriction(p, r));

    assertEquals(expected, actual);
  }
}
