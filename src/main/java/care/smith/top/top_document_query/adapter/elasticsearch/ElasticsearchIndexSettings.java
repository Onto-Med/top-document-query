package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.top_document_query.adapter.DocumentIndexSettings;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import java.util.Map;
import java.util.Objects;

public class ElasticsearchIndexSettings implements DocumentIndexSettings {

  public static Map<String, Map<String, Property>> getMappings(String language) {
    if (Objects.equals(language.toLowerCase(), "de")) {
      return Map.of(
          "properties",
          Map.of(
              "id", Property.of(p -> p.keyword(k -> k.index(true))),
              "name",
                  Property.of(
                      p ->
                          p.text(
                              t ->
                                  t.fields(
                                      "keyword",
                                      Property.of(ip -> ip.keyword(k -> k.ignoreAbove(256)))))),
              "text", Property.of(p -> p.text(t -> t.analyzer("medical_analyzer_german")))));
    } else {
      return Map.of("properties", Map.of());
    }
  }

  public static Map<String, IndexSettingsAnalysis> getSettings(String language) {
    if (Objects.equals(language.toLowerCase(), "de")) {
      return Map.of(
          "analysis",
          new IndexSettingsAnalysis.Builder()
              .filter(
                  Map.of(
                      "german_snowball",
                          new TokenFilter.Builder()
                              .definition(
                                  new TokenFilterDefinition.Builder()
                                      .snowball(s -> s.language(SnowballLanguage.German2))
                                      .build())
                              .build(),
                      "german_stop",
                          new TokenFilter.Builder()
                              .definition(
                                  new TokenFilterDefinition.Builder()
                                      .stop(s -> s.stopwords("_german_"))
                                      .build())
                              .build()))
              .analyzer(
                  Map.of(
                      "medical_analyzer_german",
                      new Analyzer.Builder()
                          .custom(
                              new CustomAnalyzer.Builder()
                                  .tokenizer("whitespace")
                                  .filter("lowercase", "german_stop", "german_snowball")
                                  .build())
                          .build()))
              .build());
    } else {
      return Map.of("analysis", new IndexSettingsAnalysis.Builder().build());
    }
  }
}
