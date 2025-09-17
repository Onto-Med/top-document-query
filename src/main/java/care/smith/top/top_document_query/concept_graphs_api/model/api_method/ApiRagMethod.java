package care.smith.top.top_document_query.concept_graphs_api.model.api_method;

public enum ApiRagMethod {
    INIT("init"),
    QUESTION("question");

    private String endpoint;

    ApiRagMethod(String endpoint) {
      this.endpoint = String.format("/rag/%s", endpoint);
    }

    public String getEndpoint() {
        return endpoint;
    }
}
