package care.smith.top.top_document_query.concept_cluster;

import care.smith.top.model.*;
import care.smith.top.top_document_query.concept_cluster.model.*;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiGraphMethod;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiPipelineMethod;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiProcessMethod;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiStatus;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineFailEntity;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineFailWithExplicit;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineResponseEntity;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineStatusEntity;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * A concept pipeline manager provides methods to view, start and delete concept cluster pipelines.
 *
 * <p>Each pipeline consists of two steps, one for generating concept graphs from a document source,
 * and one for extracting relevant clusters from these graphs.
 */
public class ConceptPipelineManager {
  public static final int DEFAULT_MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024;
  private static final Logger LOGGER = Logger.getLogger(ConceptPipelineManager.class.getName());
  private WebClient conceptGraphsApi;
  private int maxInMemorySize = DEFAULT_MAX_IN_MEMORY_SIZE;

  /**
   * Instantiate a new concept pipeline manager with the given concept-graphs endpoint. See <a
   * href="https://github.com/Onto-Med/concept-graphs">concept-graphs</a> for details.
   *
   * <p>Maximum in-memory size for requests to Elasticsearch defaults to {@link
   * #DEFAULT_MAX_IN_MEMORY_SIZE} bytes.
   *
   * @param conceptGraphApiEndpoint The concept-graphs endpoint.
   */
  public ConceptPipelineManager(String conceptGraphApiEndpoint) {
    ExchangeStrategies exchangeStrategies =
        ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxInMemorySize))
            .build();
    conceptGraphsApi =
        WebClient.builder()
            .baseUrl(conceptGraphApiEndpoint)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(exchangeStrategies)
            .build();
  }

  /**
   * Instantiate a new concept pipeline manager with the given concept-graphs endpoint. See <a
   * href="https://github.com/Onto-Med/concept-graphs">concept-graphs</a> for details.
   *
   * @param conceptGraphApiEndpoint The concept-graphs endpoint.
   * @param maxInMemorySize Maximum in-memory size in bytes for requests to Elasticsearch.
   */
  public ConceptPipelineManager(String conceptGraphApiEndpoint, int maxInMemorySize) {
    this.maxInMemorySize = maxInMemorySize;
    new ConceptPipelineManager(conceptGraphApiEndpoint);
  }

  /**
   * Get number of pipelines, including running, completed and failed once.
   *
   * @return Number of pipelines.
   */
  public long count() {
    return getAllStoredProcesses().size();
  }

  /**
   * Start a new concept pipeline with the given {@code processName}. You should at least specify a
   * {@code data} file or {@code configs} to access a remote Elasticsearch server with text
   * documents that will be processed by the pipeline.
   *
   * @param data An optional ZIP file of text documents.
   * @param configs Additional configurations for the text processing. For instance, a remote
   *     Elasticsearch container can be specified here.
   * @param labels An optional text file containing relevant labels that will be used to process the
   *     text documents.
   * @param processName Name of the process scheduled with the pipeline.
   * @param language Determines the pretrained text modules that will be used to process the text
   *     documents (available languages are 'de' and 'en').
   * @param skipPresent If a process with the given name already exists, the completed steps will be
   *     skipped and the pipeline will pick up where it left off.
   * @param returnStatistics Whether the function should return statistics about the pipeline.
   *     Setting this parameter to {@code true} forces the function to wait for the pipeline to
   *     finish.
   * @return A {@link PipelineResponseEntity} containing minimal information about the pipeline or
   *     detailed statistics.
   */
  public PipelineResponseEntity startPipeline(
      @Nullable File data,
      @Nullable Map<String, File> configs,
      @Nullable File labels,
      @Nonnull String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics) {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    if (data != null) {
      parts.add("data", new FileSystemResource(data));
    }
    return callApi(labels, processName, language, skipPresent, returnStatistics, configs, parts);
  }

  /**
   * Start a new concept pipeline with the given {@code jsonBody}. 'name' and 'language' values provided therein
   * take precedence over {@code processName} and {@code language}.
   *
   * @param processName Name of the process scheduled with the pipeline.
   * @param language Determines the pretrained text modules that will be used to process the text
   *     documents (available languages are 'de' and 'en').
   * @param skipPresent If a process with the given name already exists, the completed steps will be
   *     skipped and the pipeline will pick up where it left off.
   * @param returnStatistics Whether the function should return statistics about the pipeline.
   *     Setting this parameter to {@code true} forces the function to wait for the pipeline to
   *     finish.
   * @return A {@link PipelineResponseEntity} containing minimal information about the pipeline or
   *     detailed statistics.
   */
  public PipelineResponseEntity startPipeline(
      @Nonnull String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics,
      @Nonnull JSONObject jsonBody
  ) {
    return callApiWithJson(processName, language, skipPresent, returnStatistics, jsonBody);
  }

  /**
   *
   * @param processName Name of the pipeline/process for which the configuration should be gotten;
*        if null a default configuration will be returned (if there is one declared in the concept-graphs-api).
   * @return An optional {@link JSONObject}.
   */
  public Optional<String> getPipelineConfiguration(@Nullable String processName, @Nullable String language) {
    boolean defaultConfig;
    String lang = Objects.requireNonNullElse(language, "en");
    if (processName != null) {
      processName = processName.trim();
      defaultConfig = false;
    } else {
      processName = "default";
      defaultConfig = true;
    }

    try {
      String finalProcessName = processName;
      return Optional.ofNullable(
          conceptGraphsApi
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiPipelineMethod.CONFIG.getEndpoint())
                          .queryParam("process", finalProcessName)
                          .queryParam("default", defaultConfig)
                          .queryParam("language", lang)
                          .build())
              .retrieve()
              .bodyToMono(String.class)
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Get graphs that were constructed by the specified pipeline. You can optionally filter the
   * graphs by their ID.
   *
   * @param processName Name of the process to retrieve the graphs from.
   * @param graphIds Optional list of graph IDs to filter by.
   * @return {@link Map} of available concept graphs for the given {@code pipelineName}. The map
   *     contains graph IDs as keys.
   */
  public Map<String, ConceptGraphEntity> getConceptGraphs(
      String processName, @Nullable List<String> graphIds) {
    List<String> ids;
    if (graphIds == null || graphIds.isEmpty()) {
      ids =
          getGraphStatisticsForProcess(processName)
              .map(
                  conceptGraphStatisticsEntity ->
                      Arrays.stream(conceptGraphStatisticsEntity.getConceptGraphs())
                          .map(GraphStatsEntity::getId)
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
    } else {
      ids = new ArrayList<>(graphIds);
    }
    HashMap<String, ConceptGraphEntity> result = new HashMap<>();
    ids.forEach(
        id -> getGraphForIdAndProcess(id, processName).ifPresent(graph -> result.put(id, graph)));
    return result;
  }

  /**
   * Get a list of all processes previously scheduled to process text documents.
   *
   * @return List of processes.
   */
  public List<ConceptGraphPipeline> getAllStoredProcesses() {
    ProcessOverviewEntity processOverviewEntity = null;
    try {
      processOverviewEntity =
          conceptGraphsApi
              .get()
              .uri(uriBuilder -> uriBuilder.path(ApiProcessMethod.ALL.getEndpoint()).build())
              .retrieve()
              .bodyToMono(ProcessOverviewEntity.class)
              .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
    }
    List<ConceptGraphPipeline> conceptGraphPipelines =
        (processOverviewEntity != null ? processOverviewEntity.toApiModel() : new ArrayList<>());
    conceptGraphPipelines.forEach(
        conceptGraphPipeline -> {
          conceptGraphPipeline.getSteps().stream()
              .filter(step -> step.getName().equals(ConceptGraphPipelineStepsEnum.GRAPH))
              .forEach(
                  step -> {
                    if (step.getStatus().equals(ConceptGraphPipelineStatusEnum.FINISHED)) {
                      conceptGraphPipeline.setStatus(PipelineResponseStatus.SUCCESSFUL);
                    } else if (step.getStatus().equals(ConceptGraphPipelineStatusEnum.RUNNING)
                        || step.getStatus().equals(ConceptGraphPipelineStatusEnum.STARTED)) {
                      conceptGraphPipeline.setStatus(PipelineResponseStatus.RUNNING);
                    } else if (step.getStatus().equals(ConceptGraphPipelineStatusEnum.STOPPED)
                        || step.getStatus().equals(ConceptGraphPipelineStatusEnum.ABORTED)) {
                      conceptGraphPipeline.setStatus(PipelineResponseStatus.STOPPED);
                    } else {
                      conceptGraphPipeline.setStatus(PipelineResponseStatus.FAILED);
                    }
                  });
        });
    return conceptGraphPipelines;
  }

  /**
   * Stops a process by its id; can only stop a process after its currently running step is finished.
   *
   * @param processId The id of the process.
   * @return The server message as {@link String}.
   */
  public String stopPipeline(String processId) {
    try {
      return conceptGraphsApi
          .get()
          .uri(uriBuilder -> uriBuilder.path(ApiProcessMethod.STOP.getEndpoint(processId)).build())
          .exchangeToMono(response -> response.bodyToMono(String.class))
          .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
    }
    return "Something went wrong; check the `concept-graphs-api` logs.";
  }

  /**
   * Deletes a process by its id; can't delete a process that is running.
   *
   * @param processId The id of the process.
   * @return The server message as {@link String}.
   */
  public String deleteProcess(String processId) {
    try {
      return conceptGraphsApi
          .delete()
          .uri(
              uriBuilder -> uriBuilder.path(ApiProcessMethod.DELETE.getEndpoint(processId)).build())
          .exchangeToMono(
              response -> {
                if (ArrayUtils.contains(
                    new int[] {HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value()},
                    response.statusCode().value())) {
                  return response.bodyToMono(String.class);
                }
                return response.bodyToMono(String.class);
              })
          .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
    }
    return "Something went wrong; check the `concept-graphs-api` logs.";
  }

  /**
   * Get a single graph from a process with the specified {@code graphId}
   *
   * @param graphId Graph ID to filter by.
   * @param processName Process name to filter by.
   * @return {@link Optional} containing the graph, if there is any.
   */
  public Optional<ConceptGraphEntity> getGraphForIdAndProcess(String graphId, String processName) {
    try {
      return Optional.ofNullable(
          conceptGraphsApi
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiGraphMethod.GRAPH.getEndpoint(graphId))
                          .queryParam("process", processName)
                          .build())
              .retrieve()
              .bodyToMono(ConceptGraphEntity.class)
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Get statistics about the graphs that where constructed by a process.
   *
   * @param processName Name of the process.
   * @return {@link Optional} containing the graph statistics of a process with the given name if
   *     exists.
   */
  public Optional<ConceptGraphStatisticsEntity> getGraphStatisticsForProcess(String processName) {
    try {
      return Optional.ofNullable(
          conceptGraphsApi
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiGraphMethod.STATISTICS.getEndpoint())
                          .queryParam("process", processName)
                          .build())
              .retrieve()
              .bodyToMono(ConceptGraphStatisticsEntity.class)
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Gets the status of a specific pipeline and its sub steps
   *
   * @param processName Name of the process
   * @return {@link Optional} containing the status of a pipeline with the given name if exists.
   */
  public Optional<PipelineStatusEntity> getStatusOfProcess(String processName) {
    try {
      return Optional.ofNullable(
          conceptGraphsApi
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiStatus.SELF.getEndpoint())
                          .queryParam("process", processName)
                          .build())
              .retrieve()
              .bodyToMono(PipelineStatusEntity.class)
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  private PipelineResponseEntity callApi(
      File labels,
      String processName,
      String language,
      Boolean skipPresent,
      Boolean returnStatistics,
      Map<String, File> configs,
      MultiValueMap<String, Object> parts) {
    if (labels != null) parts.add("labels", new FileSystemResource(labels));
    if (configs != null && !configs.isEmpty())
      configs.forEach((name, file) -> parts.add(name + "_config", new FileSystemResource(file)));
    try {
      Mono<PipelineResponseEntity> apiResponse =
          conceptGraphsApi
              .post()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiPipelineMethod.INITIALIZE.getEndpoint())
                          .queryParam("process", processName)
                          .queryParam("lang", language == null ? "en" : language)
                          .queryParam("skip_present", skipPresent == null || skipPresent)
                          .queryParam(
                              "return_statistics", returnStatistics != null && returnStatistics)
                          .build())
              .body(BodyInserters.fromMultipartData(parts))
              .exchangeToMono(
                  response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                      return response.bodyToMono(ConceptGraphStatisticsEntity.class);
                    } else if (response.statusCode().equals(HttpStatus.ACCEPTED)) {
                      return response.bodyToMono(PipelineStatusEntity.class);
                    } else if (ArrayUtils.contains(
                        new int[] {
                          HttpStatus.FORBIDDEN.value(),
                          HttpStatus.NOT_FOUND.value(),
                          HttpStatus.BAD_REQUEST.value()
                        },
                        response.statusCode().value())) {
                      return response.bodyToMono(PipelineFailWithExplicit.class);
                    } else {
                      return response.bodyToMono(PipelineFailEntity.class);
                    }
                  });
      return apiResponse.block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return null;
    }
  }

  private PipelineResponseEntity callApiWithJson(
      String processName,
      String language,
      Boolean skipPresent,
      Boolean returnStatistics,
      JSONObject jsonBody) {
    try {
      Mono<PipelineResponseEntity> apiResponse =
          conceptGraphsApi
              .post()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiPipelineMethod.INITIALIZE.getEndpoint())
                          .queryParam("process", processName)
                          .queryParam("lang", language == null ? "en" : language)
                          .queryParam("skip_present", skipPresent == null || skipPresent)
                          .queryParam(
                              "return_statistics", returnStatistics != null && returnStatistics)
                          .build())
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(jsonBody.toString())
              .exchangeToMono(
                  response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                      return response.bodyToMono(ConceptGraphStatisticsEntity.class);
                    } else if (response.statusCode().equals(HttpStatus.ACCEPTED)) {
                      return response.bodyToMono(PipelineStatusEntity.class);
                    } else if (ArrayUtils.contains(
                        new int[] {
                          HttpStatus.FORBIDDEN.value(),
                          HttpStatus.NOT_FOUND.value(),
                          HttpStatus.BAD_REQUEST.value()
                        },
                        response.statusCode().value())) {
                      return response.bodyToMono(PipelineFailWithExplicit.class);
                    } else {
                      return response.bodyToMono(PipelineFailEntity.class);
                    }
                  });
      return apiResponse.block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return null;
    }
  }
}
