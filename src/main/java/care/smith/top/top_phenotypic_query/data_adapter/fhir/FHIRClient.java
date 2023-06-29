package care.smith.top.top_phenotypic_query.data_adapter.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder.FHIRPatientFinder;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder.FHIRPhenotypeFinder;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder.FHIRResourceFinder;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FHIRClient {

  private IGenericClient client;
  private FHIRPath path;
  private static final Logger log = LoggerFactory.getLogger(FHIRClient.class);

  public FHIRClient(DataAdapterConfig config) {
    FhirContext ctx = FhirContext.forR4();
    ctx.getRestfulClientFactory().setConnectTimeout(2 * 60 * 60 * 1000);
    ctx.getRestfulClientFactory().setSocketTimeout(2 * 60 * 60 * 1000);
    this.client = ctx.newRestfulGenericClient(config.getConnectionAttribute("url"));

    String user = config.getConnectionAttribute("user");
    String password = config.getConnectionAttribute("password");
    String token = config.getConnectionAttribute("token");

    if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password))
      client.registerInterceptor(new BasicAuthInterceptor(user, password));
    else if (StringUtils.isNotBlank(token))
      client.registerInterceptor(new BearerTokenAuthInterceptor(token));

    this.path = new FHIRPath(ctx);
  }

  public List<Resource> findResources(String query) {
    List<Resource> resources = new ArrayList<>();
    new FHIRResourceFinder(client, resources).findResources(query);
    return resources;
  }

  public ResultSet findPatients(String query, SubjectSearch search) {
    ResultSet rs = new ResultSet();
    new FHIRPatientFinder(client, path, rs, search).findResources(query);
    return rs;
  }

  public ResultSet findPhenotypes(String query, SingleSearch search) {
    ResultSet rs = new ResultSet();
    new FHIRPhenotypeFinder(client, path, rs, search).findResources(query);
    return rs;
  }

  public Optional<String> createResource(Resource res) {
    log.info("CREATE RESOURCE: {}", FHIRUtil.toString(res));
    return create(client.create().resource(res).execute());
  }

  public boolean deleteResource(String resourceType, String idPart) {
    log.info("DELETE {}:{}", resourceType.toUpperCase(), idPart);
    return delete(
        client
            .delete()
            .resourceById(new IdDt(resourceType, idPart))
            .execute()
            .getOperationOutcome());
  }

  public Optional<Resource> findResource(String resourceType, String idPart) {
    Bundle b =
        client.search().byUrl(resourceType + "/" + idPart).returnBundle(Bundle.class).execute();
    if (b.getTotal() == 0) return Optional.empty();
    else return Optional.of(b.getEntryFirstRep().getResource());
  }

  public Optional<Resource> findResourceOfSubject(
      String resourceType, String codeUri, String patientIdPart) {
    Bundle b = findResourcesOfSubject(resourceType, codeUri, patientIdPart);
    if (b.getTotal() == 0) return Optional.empty();
    else return Optional.of(b.getEntryFirstRep().getResource());
  }

  public Bundle findResourcesOfSubject(String resourceType, String codeUri, String patientIdPart) {
    return search(resourceType + "?subject=Patient/" + patientIdPart + "&code=" + codeUri);
  }

  public Bundle findResourcesOfSubject(String resourceType, String patientIdPart) {
    return search(resourceType + "?subject=Patient/" + patientIdPart);
  }

  public Optional<String> findResourceId(String resourceType, String system, String identifier) {
    Bundle b = search(resourceType + "?identifier=" + system + "|" + identifier);
    if (b.getTotal() == 0) return Optional.empty();
    else return Optional.of(b.getEntryFirstRep().getResource().getIdElement().getIdPart());
  }

  public void deleteAllResources(String system) {
    deleteAllResourcesOfType("Condition", system);
    deleteAllResourcesOfType("Procedure", system);
    deleteAllResourcesOfType("MedicationRequest", system);
    deleteAllResourcesOfType("MedicationAdministration", system);
    deleteAllResourcesOfType("MedicationStatement", system);
    deleteAllResourcesOfType("Medication", system);
    deleteAllResourcesOfType("ClinicalImpression", system);
    deleteAllResourcesOfType("Observation", system);
    deleteAllResourcesOfType("AllergyIntolerance", system);
    deleteAllResourcesOfType("Patient", system);
  }

  public boolean deleteAllResourcesOfType(String resourceType, String system) {
    log.info("DELETE ALL {}S:", resourceType.toUpperCase());
    MethodOutcome resp =
        client
            .delete()
            .resourceConditionalByUrl(resourceType + "?identifier=" + system + "|")
            .execute();
    return delete(resp.getOperationOutcome());
  }

  private Optional<String> create(MethodOutcome outcome) {
    if (outcome != null && outcome.getCreated() != null && outcome.getCreated().booleanValue()) {
      String id = outcome.getId().getIdPart();
      log.info("SUCCESSFULLY CREATED (ID:{})!", id);
      return Optional.of(id);
    } else {
      log.warn("CREATION FAILED!");
      return Optional.empty();
    }
  }

  private boolean delete(IBaseOperationOutcome resp) {
    if (resp == null) {
      log.warn("NO RESPONSE");
      return false;
    } else {
      OperationOutcome outcome = (OperationOutcome) resp;
      for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
        if (issue.getCode() != IssueType.INFORMATIONAL) {
          log.warn(outcome.getIssueFirstRep().getDiagnostics());
          return false;
        } else log.info(outcome.getIssueFirstRep().getDiagnostics());
      }
      return true;
    }
  }

  private Bundle search(String url) {
    return client.search().byUrl(url).returnBundle(Bundle.class).execute();
  }
}
