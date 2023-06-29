package care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRPath;
import care.smith.top.top_phenotypic_query.result.ResultSet;

public abstract class FHIRPathResourceFinder extends FHIRAbstractResourceFinder {

  protected FHIRPath path;
  protected ResultSet rs;

  protected FHIRPathResourceFinder(IGenericClient client, FHIRPath path, ResultSet rs) {
    super(client);
    this.path = path;
    this.rs = rs;
  }
}
