package care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import org.hl7.fhir.r4.model.Resource;

public class FHIRResourceFinder extends FHIRAbstractResourceFinder {

  private List<Resource> rs;

  public FHIRResourceFinder(IGenericClient client, List<Resource> rs) {
    super(client);
    this.rs = rs;
  }

  @Override
  protected void addResource(Resource res) {
    rs.add(res);
  }
}
