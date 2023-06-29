package care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

public abstract class FHIRAbstractResourceFinder {

  protected IGenericClient client;

  protected FHIRAbstractResourceFinder(IGenericClient client) {
    this.client = client;
  }

  public void findResources(String query) {
    Bundle firstPage =
        client
            .search()
            .byUrl(query)
            .returnBundle(Bundle.class)
            .cacheControl(new CacheControlDirective().setNoCache(true))
            .execute();
    addResources(firstPage);
    addPages(firstPage);
  }

  private void addResources(Bundle bundle) {
    for (BundleEntryComponent bec : bundle.getEntry()) addResource(bec.getResource());
  }

  private void addPages(Bundle lastPage) {
    if (lastPage.getLink(Bundle.LINK_NEXT) == null) return;
    Bundle nextPage = client.loadPage().next(lastPage).execute();
    addResources(nextPage);
    addPages(nextPage);
  }

  protected abstract void addResource(Resource res);
}
