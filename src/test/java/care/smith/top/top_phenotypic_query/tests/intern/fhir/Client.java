package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRClient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class Client {

  protected static final String SYSTEM = "https://www.top-test.de/system";
  private static FHIRClient client;

  public Client(String configResourcePath) {
    client = new FHIRClient(DataAdapterConfig.getInstanceFromResource(configResourcePath));
  }

  public Client(String url, String user, String password, String token) {
    DataAdapterConfig config = new DataAdapterConfig();
    config.setConnectionAttribute("url", url);
    config.setConnectionAttribute("user", user);
    config.setConnectionAttribute("password", password);
    config.setConnectionAttribute("token", token);
    client = new FHIRClient(config);
  }

  public Client() {
    DataAdapterConfig config = new DataAdapterConfig();
    config.setConnectionAttribute("url", "http://localhost:8080/baseR4");
    client = new FHIRClient(config);
  }

  public void clean() {
    client.deleteAllResources(SYSTEM);
  }

  public Reference add(Resource r) {
    return new Reference(r.getResourceType() + "/" + client.createResource(r).get());
  }
}
