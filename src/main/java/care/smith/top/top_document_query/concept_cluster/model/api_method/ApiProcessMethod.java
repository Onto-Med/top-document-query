package care.smith.top.top_document_query.concept_cluster.model.api_method;

public enum ApiProcessMethod {
  ALL("/processes"),
  STOP("/stop"),
  DELETE("/delete");

  private String endpoint;

  ApiProcessMethod(String endpoint) {
    this.setEndpoint(endpoint);
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getEndpoint(String processId) {
    if (endpoint.equals(DELETE.endpoint) || endpoint.equals(STOP.endpoint)) {
      return String.format("%s/%s%s", ALL.endpoint, processId, endpoint);
    }
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
