package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.top_document_query.adapter.DocumentIndexSettings;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class ElasticsearchIndexSettings implements DocumentIndexSettings {

    public static @Nullable Map<String, Object> getMappings(String language) {
        if (Objects.equals(language.toLowerCase(), "de")) {
            return Map.of("properties", Map.of(
                    "id", Property.of(p -> p.keyword(k -> k.index(true))),
                    "name", Property.of(p -> p.text(t -> t.fields("keyword", Property.of(ip -> ip.keyword(k -> k.ignoreAbove(256))))))),
                    "text", Property.of(p -> p.text(t -> t.analyzer("medical_analyzer_german")))
            );
        } else {
            return null;
        }
    }

    public @Nullable Map<String, Map<String, JSONObject>> getSettings(String language) {
        if (Objects.equals(language.toLowerCase(), "de")) {
            return Map.of("analysis",
                    Map.of("analyzer", new JSONObject(
                                    """
                                            {
                                            'analyzer': {
                                                'medical_analyzer_german': {
                                                    'tokenizer': 'whitespace',
                                                    'filter': ['lowercase', 'german_stop', 'german_snowball']
                                                },
                                            },
                                            'filter': {
                                                'german_snowball': {'type': 'snowball', 'language': 'German2'},
                                                'german_stop': {'type': 'stop', 'stopwords': '_german_'},
                                            }
                                            }
                                            """
                            )
                    )
            );
        } else  {
            return null;
        }
    }
}
