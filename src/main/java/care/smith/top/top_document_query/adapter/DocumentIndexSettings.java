package care.smith.top.top_document_query.adapter;

import org.json.JSONObject;

import javax.annotation.Nullable;
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
