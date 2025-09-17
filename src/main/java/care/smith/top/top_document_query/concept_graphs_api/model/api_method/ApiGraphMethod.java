package care.smith.top.top_document_query.concept_graphs_api.model.api_method;

import java.util.Objects;

public enum ApiGraphMethod {
  CREATION("/graph/creation"),
  STATISTICS("/graph/statistics"),
  GRAPH("/graph/");

  private String endpoint;

  ApiGraphMethod(String endpoint) {
    this.setEndpoint(endpoint);
  }

  public String getEndpoint(String graphId) {
    if (Objects.equals(endpoint, GRAPH.endpoint)) return endpoint + graphId;
    return endpoint;
  }

  public String getEndpoint() {
    if (Objects.equals(endpoint, GRAPH.endpoint)) return endpoint + 0;
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
