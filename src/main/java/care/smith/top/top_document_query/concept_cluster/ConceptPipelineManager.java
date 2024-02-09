package care.smith.top.top_document_query.concept_cluster;

import care.smith.top.top_document_query.concept_cluster.model.*;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiGraphMethod;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiPipelineMethod;
import care.smith.top.top_document_query.concept_cluster.model.api_method.ApiProcessMethod;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineFailEntity;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineFailWithExplicit;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineResponseEntity;
import care.smith.top.top_document_query.concept_cluster.model.pipeline_response.PipelineStatusEntity;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
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
  private static final Logger LOGGER = Logger.getLogger(ConceptPipelineManager.class.getName());
  private WebClient conceptGraphsApi;
  private int maxInMemorySize = 16 * 1024 * 1024;

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
    ProcessOverviewEntity processes = getAllStoredProcesses();
    return processes != null ? Arrays.stream(processes.getProcesses()).count() : 0;
  }

  public PipelineResponseEntity startPipelineForData(
      @Nonnull File data,
      @Nonnull String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics) {
    return startPipelineForDataAndLabelsAndConfigs(
        data, null, processName, language, skipPresent, returnStatistics, null);
  }

  public PipelineResponseEntity startPipelineForDataAndLabels(
      @Nonnull File data,
      @Nonnull File labels,
      @Nonnull String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics) {
    return startPipelineForDataAndLabelsAndConfigs(
        data, labels, processName, language, skipPresent, returnStatistics, null);
  }

  public PipelineResponseEntity startPipelineForDataAndConfigs(
      @Nonnull File data,
      @Nullable String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics,
      @Nonnull Map<String, File> configs) {
    return startPipelineForDataAndLabelsAndConfigs(
        data, null, processName, language, skipPresent, returnStatistics, configs);
  }

  public PipelineResponseEntity startPipelineForDataAndLabelsAndConfigs(
      @Nonnull File data,
      File labels,
      @Nullable String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics,
      Map<String, File> configs) {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("data", new FileSystemResource(data));
    return callApi(labels, processName, language, skipPresent, returnStatistics, configs, parts);
  }

  public PipelineResponseEntity startPipelineForDataServerAndLabelsAndConfigs(
      File labels,
      @Nullable String processName,
      @Nullable String language,
      @Nullable Boolean skipPresent,
      @Nullable Boolean returnStatistics,
      Map<String, File> configs) {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    return callApi(labels, processName, language, skipPresent, returnStatistics, configs, parts);
  }

  private ProcessOverviewEntity getAllStoredProcesses() {
    try {
      return conceptGraphsApi
          .get()
          .uri(uriBuilder -> uriBuilder.path(ApiProcessMethod.ALL.getEndpoint()).build())
          .retrieve()
          .bodyToMono(ProcessOverviewEntity.class)
          .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return null;
    }
  }

  private ConceptGraphEntity getGraphForIdAndProcess(String id, String processName) {
    try {
      return conceptGraphsApi
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(ApiGraphMethod.GRAPH.getEndpoint(id))
                      .queryParam("process", processName)
                      .build())
          .retrieve()
          .bodyToMono(ConceptGraphEntity.class)
          .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return null;
    }
  }

  private ConceptGraphStatisticsEntity getGraphStatisticsForProcess(String processName) {
    try {
      return conceptGraphsApi
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(ApiGraphMethod.STATISTICS.getEndpoint())
                      .queryParam("process", processName)
                      .build())
          .retrieve()
          .bodyToMono(ConceptGraphStatisticsEntity.class)
          .block();
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return null;
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
}
