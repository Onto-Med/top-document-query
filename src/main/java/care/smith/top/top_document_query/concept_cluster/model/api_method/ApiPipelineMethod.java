package care.smith.top.top_document_query.concept_cluster.model.api_method;

public enum ApiPipelineMethod {
  INITIALIZE("/pipeline"),
  CONFIG("/pipeline/configuration");

  private String endpoint;

  ApiPipelineMethod(String endpoint) {
    this.setEndpoint(endpoint);
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
