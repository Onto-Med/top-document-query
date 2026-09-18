package care.smith.top.top_document_query.concept_graphs_api;

import care.smith.top.top_document_query.concept_graphs_api.model.QueryExpansionProfileEntity;
import care.smith.top.top_document_query.concept_graphs_api.model.QueryExpansionProfileListEntity;
import care.smith.top.top_document_query.concept_graphs_api.model.api_method.ApiQueryExpansionMethod;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class QueryExpansionManager extends AbstractExternalManager {
  private static final Logger LOGGER = Logger.getLogger(QueryExpansionManager.class.getName());

  public QueryExpansionManager(String conceptGraphApiEndpoint)
      throws MalformedURLException, URISyntaxException {
    super(conceptGraphApiEndpoint, LOGGER);
  }

  public QueryExpansionManager(String conceptGraphApiEndpoint, int memorySize)
      throws MalformedURLException, URISyntaxException {
    super(conceptGraphApiEndpoint, memorySize, LOGGER);
  }

  /**
   * List semantic query-expansion profile metadata from the Concept Graphs API.
   *
   * @return {@link Optional} containing profile metadata if available.
   */
  public Optional<List<QueryExpansionProfileEntity>> getProfiles() {
    try {
      QueryExpansionProfileListEntity response =
          conceptGraphsApi
              .get()
              .uri(ApiQueryExpansionMethod.PROFILES.getEndpoint())
              .retrieve()
              .bodyToMono(QueryExpansionProfileListEntity.class)
              .block();
      return Optional.ofNullable(response).map(QueryExpansionProfileListEntity::getProfiles);
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Get semantic query-expansion profile metadata from the Concept Graphs API.
   *
   * @param profileName Name of the query-expansion profile.
   * @return {@link Optional} containing the profile metadata if available.
   */
  public Optional<QueryExpansionProfileEntity> getProfile(String profileName) {
    try {
      return Optional.ofNullable(
          conceptGraphsApi
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(ApiQueryExpansionMethod.PROFILE.getEndpoint())
                          .build(profileName))
              .retrieve()
              .bodyToMono(QueryExpansionProfileEntity.class)
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Generate query-expansion candidates through the Concept Graphs API.
   *
   * @param request Concept Graphs query-expansion request payload.
   * @return {@link Optional} containing the response payload if available.
   */
  public Optional<Map<String, Object>> expand(Map<String, Object> request) {
    try {
      return Optional.ofNullable(
          conceptGraphsApi
              .post()
              .uri(ApiQueryExpansionMethod.EXPAND.getEndpoint())
              .bodyValue(request)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
              .block());
    } catch (WebClientResponseException e) {
      LOGGER.warning(e.getResponseBodyAsString() + " -- " + e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  String getSubclassName() {
    return "Query Expansion Manager";
  }
}
