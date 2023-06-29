package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.Entity;
import care.smith.top.model.EntityPage;
import care.smith.top.model.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HTTP {

  public static String getToken(String user, String pw) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://top.imise.uni-leipzig.de/auth/realms/top-realm/protocol/openid-connect/token"))
            .POST(
                BodyPublishers.ofString(
                    "client_id=top-frontend&username="
                        + user
                        + "&password="
                        + pw
                        + "&grant_type=password"))
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.readTree(response.body());
    return json.get("access_token").textValue();
  }

  private static <T> T read(String url, String token, Class<T> cls)
      throws IOException, InterruptedException {
    URLConnection uc = new URL(url).openConnection();
    uc.setRequestProperty("Authorization", "Bearer " + token);
    String text = new String(uc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    return mapper.readValue(text, cls);
  }

  public static List<Entity> readEntities(String repoUrl, String token)
      throws IOException, InterruptedException {
    EntityPage page1 = HTTP.read(repoUrl + "/entity", token, EntityPage.class);
    List<Entity> entities = page1.getContent();
    for (int i = 2; i <= page1.getTotalPages(); i++) {
      EntityPage pageN = HTTP.read(repoUrl + "/entity?page=" + i, token, EntityPage.class);
      entities.addAll(pageN.getContent());
    }
    return entities;
  }

  public static Repository readRepository(String repoUrl, String token)
      throws IOException, InterruptedException {
    return HTTP.read(repoUrl, token, Repository.class);
  }
}
