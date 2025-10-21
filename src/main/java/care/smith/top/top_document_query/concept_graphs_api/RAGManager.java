package care.smith.top.top_document_query.concept_graphs_api;

import care.smith.top.model.RAGAnswer;
import care.smith.top.model.RAGStatus;
import care.smith.top.top_document_query.concept_graphs_api.model.api_method.ApiRagMethod;
import care.smith.top.top_document_query.concept_graphs_api.model.api_method.ApiStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.MalformedURLException;
import java.util.function.Function;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public class RAGManager extends AbstractExternalManager {
  private static final Logger LOGGER = Logger.getLogger(RAGManager.class.getName());

  public RAGManager(String conceptGraphApiEndpoint) throws MalformedURLException {
    super(conceptGraphApiEndpoint, LOGGER);
  }

  public RAGManager(String conceptGraphApiEndpoint, int memorySize) throws MalformedURLException {
    super(conceptGraphApiEndpoint, memorySize, LOGGER);
  }

  public RAGAnswer poseQuestion(String process, String question) {
    Mono<RAGAnswer> apiResponse =
        conceptGraphsApi
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiRagMethod.QUESTION.getEndpoint())
                        .queryParam("process", process)
                        .queryParam("q", question)
                        .build())
            .exchangeToMono(responseToRAGAnswer);
    return apiResponse.block();
  }

  public RAGAnswer poseQuestion(String process, String question, String[] filter_list) {
    class FilterIds {
      @JsonProperty("doc_ids")
      String[] docIds;

      FilterIds addIds(String[] ids) {
        this.docIds = ids;
        return this;
      }
    }

    FilterIds filterIds = new FilterIds().addIds(filter_list);

    Mono<RAGAnswer> apiResponse =
        conceptGraphsApi
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiRagMethod.QUESTION.getEndpoint())
                        .queryParam("process", process)
                        .queryParam("q", question)
                        .build())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(filterIds), FilterIds.class)
            .exchangeToMono(responseToRAGAnswer);
    return apiResponse.block();
  }

  private final Function<ClientResponse, Mono<RAGAnswer>> responseToRAGAnswer =
      (response) -> {
        if (response.statusCode().equals(HttpStatus.OK)) {
          return response.bodyToMono(RAGAnswer.class);
        } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
          return Mono.fromSupplier(
              () ->
                  new RAGAnswer()
                      .answer("No active and ready rag component found.")
                      .info(
                          "You need to initialize it first and wait for it to be ready with '/document/rag/init' endpoint."));
        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
          return Mono.fromSupplier(
              () ->
                  new RAGAnswer()
                      .answer("Either no question was posed or method not supported; use GET!"));
        } else {
          return Mono.fromSupplier(
              () ->
                  new RAGAnswer()
                      .answer(
                          "Something went wrong. Please see the logs of 'concept-graphs-api'."));
        }
      };

  public String initRag(String process, boolean force, JSONObject jsonBody) {
    Mono<String> apiResponse =
        conceptGraphsApi
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiRagMethod.INIT.getEndpoint())
                        .queryParam("process", process)
                        .queryParam("force", force)
                        .build())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonBody.toString())
            .exchangeToMono(response -> response.bodyToMono(String.class));
    return apiResponse.block();
  }

  public RAGStatus getRAGStatus(String process) {
      Mono<RAGStatus> apiResponse =
              conceptGraphsApi
                      .get()
                      .uri(
                              uriBuilder ->
                                      uriBuilder
                                              .path(ApiStatus.RAG.getEndpoint())
                                              .queryParam("process", process)
                                              .build())
                      .exchangeToMono(
                              response -> response.bodyToMono(RAGStatus.class)
                      );

      return apiResponse.block();
  }
}
