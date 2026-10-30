package care.smith.top.top_document_query.adapter.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.List;

public class Connection {
  private String url;
  private String alternateUrl;
  private String port;
  private List<String> endpoints;

  /** Legacy accessors are retained for backend integrations during the v2 migration. */
  public String getUrl() {
    return endpoints == null ? url : baseUrl(validateEndpoint(endpoints.getFirst()));
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAlternateUrl() {
    return endpoints == null
        ? alternateUrl
        : endpoints.size() < 2 ? null : baseUrl(validateEndpoint(endpoints.get(1)));
  }

  public void setAlternateUrl(String alternateUrl) {
    this.alternateUrl = alternateUrl;
  }

  public String getPort() {
    if (endpoints == null) return port;
    URI endpoint = validateEndpoint(endpoints.getFirst());
    return String.valueOf(endpoint.getPort() < 0 ? defaultPort(endpoint) : endpoint.getPort());
  }

  public void setPort(String port) {
    this.port = port;
  }

  /** Ordered complete HTTP(S) URIs, including per-endpoint ports. */
  public List<String> getEndpoints() {
    if (endpoints != null) return endpoints;
    if (url == null) throw new IllegalArgumentException("connection.url is required");
    String primary = legacyEndpoint(url);
    return alternateUrl == null ? List.of(primary) : List.of(primary, legacyEndpoint(alternateUrl));
  }

  public void setEndpoints(List<String> endpoints) {
    if (endpoints == null || endpoints.isEmpty()) {
      throw new IllegalArgumentException("connection.endpoints must not be empty");
    }
    endpoints.forEach(Connection::validateEndpoint);
    if (endpoints.stream().distinct().count() != endpoints.size()) {
      throw new IllegalArgumentException("connection.endpoints must be unique");
    }
    this.endpoints = List.copyOf(endpoints);
  }

  @JsonIgnore
  boolean hasExplicitEndpoints() {
    return endpoints != null;
  }

  @JsonIgnore
  boolean hasLegacyAddress() {
    return url != null || alternateUrl != null || port != null;
  }

  public void validate() {
    if (hasExplicitEndpoints() && hasLegacyAddress()) {
      throw new IllegalArgumentException("Do not mix endpoints with legacy URL/port keys");
    }
    getEndpoints().forEach(Connection::validateEndpoint);
  }

  /** Paths/credentials/query parameters are rejected rather than silently discarded by clients. */
  public static URI validateEndpoint(String endpoint) {
    try {
      URI uri = URI.create(endpoint);
      if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
          || uri.getHost() == null
          || uri.getUserInfo() != null
          || uri.getQuery() != null
          || uri.getFragment() != null
          || (uri.getRawPath() != null
              && !uri.getRawPath().isEmpty()
              && !"/".equals(uri.getRawPath()))
          || uri.getRawAuthority().endsWith(":")
          || uri.getPort() == 0
          || uri.getPort() > 65535) {
        throw new IllegalArgumentException();
      }
      return uri;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(
          "Endpoint must be an HTTP(S) URI with a host and valid port, without credentials or a path");
    }
  }

  private String legacyEndpoint(String address) {
    URI uri = validateEndpoint(address.contains("://") ? address : "http://" + address);
    int endpointPort;
    try {
      endpointPort = port == null ? uri.getPort() : Integer.parseInt(port);
      if (port != null && (endpointPort < 1 || endpointPort > 65535)) {
        throw new IllegalArgumentException("connection.port must be between 1 and 65535");
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("connection.port must be an integer");
    }
    String result = baseUrl(uri) + (endpointPort < 0 ? "" : ":" + endpointPort);
    return validateEndpoint(result).toString();
  }

  private static String baseUrl(URI uri) {
    return uri.getScheme() + "://" + uri.getHost();
  }

  private static int defaultPort(URI uri) {
    return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
  }

  @Override
  public String toString() {
    return "Connection [endpoints=" + getEndpoints() + "]";
  }
}
