package care.smith.top.top_document_query.concept_graphs_api;

import care.smith.top.top_document_query.concept_graphs_api.model.QueryExpansionProfileEntity;
import care.smith.top.top_document_query.concept_graphs_api.model.api_method.ApiQueryExpansionMethod;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Logger;
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

  @Override
  String getSubclassName() {
    return "Query Expansion Manager";
  }
}
