package care.smith.top.top_document_query.concept_graphs_api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public abstract class AbstractExternalManager {
    WebClient conceptGraphsApi;

    private static final int DEFAULT_MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024;
    private Logger logger;
    private int maxInMemorySize = DEFAULT_MAX_IN_MEMORY_SIZE;
    private URL currentUrl;
    private URL defaultUrl;

    public URL getCurrentUrl() {
        return currentUrl;
    }

    public URL getDefaultUrl() {
        return defaultUrl;
    }

    /**
     * Instantiate a new concept pipeline manager with the given concept-graphs endpoint. See <a
     * href="https://github.com/Onto-Med/concept-graphs">concept-graphs</a> for details.
     *
     * <p>Maximum in-memory size for requests to Elasticsearch defaults to {@link
     * #DEFAULT_MAX_IN_MEMORY_SIZE} bytes.
     *
     * @param conceptGraphApiEndpoint The concept-graphs endpoint.
     */
    AbstractExternalManager(String conceptGraphApiEndpoint, Logger logger) throws MalformedURLException {
        init(conceptGraphApiEndpoint, maxInMemorySize, logger);
    }

    /**
     * Instantiate a new concept pipeline manager with the given concept-graphs endpoint. See <a
     * href="https://github.com/Onto-Med/concept-graphs">concept-graphs</a> for details.
     *
     * @param conceptGraphApiEndpoint The concept-graphs endpoint.
     * @param memorySize Maximum in-memory size in bytes for requests to Elasticsearch.
     */
    AbstractExternalManager(String conceptGraphApiEndpoint, int memorySize, Logger logger)
            throws MalformedURLException {
        this.maxInMemorySize = memorySize;
        init(conceptGraphApiEndpoint, memorySize, logger);
    }

    private void init(String conceptGraphApiEndpoint, Integer memory, Logger logger) throws MalformedURLException {
        this.logger = logger;
        this.currentUrl = new URL(conceptGraphApiEndpoint);
        this.defaultUrl = new URL(conceptGraphApiEndpoint);
        ExchangeStrategies exchangeStrategies =
                ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(memory))
                        .build();
        conceptGraphsApi =
                WebClient.builder()
                        .baseUrl(conceptGraphApiEndpoint)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .exchangeStrategies(exchangeStrategies)
                        .build();
    }

    public boolean isAccessible() {
        try {
            conceptGraphsApi.get().retrieve().bodyToMono(String.class).block();
            return true;
        } catch (WebClientResponseException e) {
            logger.severe(
                    String.format(
                            "Pipeline Manager at '%s' doesn't seem to be accessible.", this.currentUrl));
            return false;
        }
    }

    /**
     * Switches to a new base url for the Concept Graphs API endpoint.
     *
     * @param conceptGraphApiEndpoint The new concept-graphs endpoint.
     * @return {@code boolean} whether change was successful or not.
     */
    public boolean switchConnection(String conceptGraphApiEndpoint) throws MalformedURLException {
        if (!(new URL(conceptGraphApiEndpoint)).sameFile(this.currentUrl)) {
            URL tmpUrl = this.currentUrl;
            try {
                this.currentUrl = new URL(conceptGraphApiEndpoint);
                this.conceptGraphsApi =
                        this.conceptGraphsApi.mutate().baseUrl(conceptGraphApiEndpoint).build();
                if (!isAccessible()) {
                    this.currentUrl = tmpUrl;
                    this.conceptGraphsApi = this.conceptGraphsApi.mutate().baseUrl(tmpUrl.toString()).build();
                    return false;
                }
                ;
                logger.info("New base url is: " + "'" + conceptGraphApiEndpoint + "'.");
                return true;
            } catch (Exception e) {
                logger.warning(
                        String.format(
                                "Couldn't change to new base url: '%s'; using the previous one: '%s'.",
                                conceptGraphApiEndpoint, tmpUrl.toString()));
                this.conceptGraphsApi = this.conceptGraphsApi.mutate().baseUrl(tmpUrl.toString()).build();
            }
        } else {
            logger.info("New base url is the same as the current one.");
            return true;
        }
        return false;
    }
}
