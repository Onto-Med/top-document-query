package care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeOutput;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRPath;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.Resource;

public class FHIRPhenotypeFinder extends FHIRPathResourceFinder {

  private SingleSearch search;
  private PhenotypeOutput out;
  private Phenotype phe;
  private String phePathExp;

  public FHIRPhenotypeFinder(
      IGenericClient client, FHIRPath path, ResultSet rs, SingleSearch search) {
    super(client, path, rs);
    this.search = search;
    this.out = search.getOutput();
    this.phe = search.getPhenotype();
    this.phePathExp = out.getValue(Phenotypes.getDataType(phe));
  }

  @Override
  protected void addResource(Resource res) {
    String sbj = path.getString(res, out.getSubject());
    LocalDateTime date = path.getDateTime(res, out.getDateTime());
    LocalDateTime startDate = path.getDateTime(res, out.getStartDateTime());
    LocalDateTime endDate = path.getDateTime(res, out.getEndDateTime());

    if (Phenotypes.hasBooleanType(phe)) {
      rs.addValue(sbj, phe, search.getDateTimeRestriction(), Val.ofTrue(date, startDate, endDate));
      return;
    }

    Value val = null;

    if (Phenotypes.hasDateTimeType(phe))
      val = Val.of(path.getDateTime(res, phePathExp), date, startDate, endDate);
    else if (Phenotypes.hasNumberType(phe))
      val = Val.of(path.getNumber(res, phePathExp), date, startDate, endDate);
    else val = Val.of(path.getString(res, phePathExp), date, startDate, endDate);
    if (val != null)
      rs.addValueWithRestriction(
          sbj,
          phe,
          search.getDateTimeRestriction(),
          val,
          search.getSourceUnit(),
          search.getModelUnit());
  }
}
