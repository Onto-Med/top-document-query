package care.smith.top.top_document_query.adapter.config;

public class Connection {
  private String url;
  private String alternateUrl;
  private String port;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAlternateUrl() {
    return alternateUrl;
  }

  public void setAlternateUrl(String alternateUrl) {
    this.alternateUrl = alternateUrl;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  @Override
  public String toString() {
    return "Connection [url=" + url + ", port=" + port + ", alternateUrl=" + alternateUrl + "]";
  }
}
