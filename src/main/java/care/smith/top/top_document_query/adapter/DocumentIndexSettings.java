package care.smith.top.top_document_query.adapter;

import jakarta.annotation.Nullable;
import java.util.Map;

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
