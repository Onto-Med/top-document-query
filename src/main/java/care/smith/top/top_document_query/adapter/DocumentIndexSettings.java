package care.smith.top.top_document_query.adapter;


import java.util.Map;
import javax.annotation.Nullable;

public interface DocumentIndexSettings {
  @Nullable
  static Map<String, Object> getMappings(String language) {
    return null;
  }

  @Nullable
  static Map<String, Object> getSettings(String language) {
    return null;
  }
}
