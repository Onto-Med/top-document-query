package care.smith.top.top_document_query.adapter.config;

public class GraphDBConfig {
  private Connection connection;
  private Authentication authentication;

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }
}
