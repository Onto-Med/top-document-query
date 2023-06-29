package care.smith.top.top_phenotypic_query.tests.intern;

import care.smith.top.model.PhenotypeQuery;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.PhenotypeFinder;
import care.smith.top.top_phenotypic_query.tests.AbstractTest;
import care.smith.top.top_phenotypic_query.util.Entities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

public class POLARTestIntern extends AbstractTest {

  public static void main(String[] args)
      throws SQLException,
          MalformedURLException,
          IOException,
          InstantiationException,
          InterruptedException {
    Entities entities =
        Entities.of(
            "http://top-prod.imise.uni-leipzig.de/api/polar/delir/entity",
            System.getenv("POLAR_USER"),
            System.getenv("POLAR_PASSWORD"));

    DataAdapter adapter = DataAdapter.getInstance("test_files/POLAR_SQL_Adapter_Test_intern.yml");

    QueryCriterion cri1 =
        (QueryCriterion)
            new QueryCriterion()
                .inclusion(true)
                .defaultAggregationFunctionId(defAgrFunc.getId())
                .subjectId(entities.getPhenotypeWithTitle("Extended algorithm").getId());
    PhenotypeQuery query = new PhenotypeQuery().addCriteriaItem(cri1);

    PhenotypeFinder pf = new PhenotypeFinder(query, entities, adapter);
    ResultSet rs = pf.execute();
    System.out.println(rs.toString(entities));
    System.out.println(rs.size());
    adapter.close();

    //    System.out.println(phens.getIdsAndTitles());
    //    System.out.println(phens.size());
  }
}
