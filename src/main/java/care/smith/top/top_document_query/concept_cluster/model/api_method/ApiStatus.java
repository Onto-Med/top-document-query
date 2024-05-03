package care.smith.top.top_document_query.concept_cluster.model.api_method;

public enum ApiStatus {
  SELF("/status"),
  DOCUMENT_SERVER("/status/document-server");

  private String endpoint;

  ApiStatus(String endpoint) {
    this.setEndpoint(endpoint);
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
