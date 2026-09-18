package care.smith.top.top_document_query.concept_graphs_api.model.api_method;

public enum ApiQueryExpansionMethod {
  PROFILES("/query-expansion/profiles"),
  PROFILE("/query-expansion/profiles/{profileName}"),
  EXPAND("/query-expansion");

  private String endpoint;

  ApiQueryExpansionMethod(String endpoint) {
    this.setEndpoint(endpoint);
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
