package care.smith.top.top_document_query.util.builder;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.TextAdapter;
import care.smith.top.top_document_query.adapter.TextFinder;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;

public class CQue {

  private TextAdapter adapter;
  private TextAdapterConfig config;
  private ConceptQuery query;
  private Entity[] entities;

  public CQue(
      TextAdapter adapter,
      TextAdapterConfig config,
      Entity[] entities,
      String parentCatId,
      String lang) {
    this.adapter = adapter;
    this.config = config;
    this.query = new ConceptQuery().entityId(parentCatId).language(lang);
    this.entities = entities;
  }

  public static TextAdapter getAdapter(String configFilePath) throws InstantiationException {
    return TextAdapter.getInstance(getConfig(configFilePath));
  }

  public static TextAdapterConfig getConfig(String configFilePath) {
    return TextAdapterConfig.getInstance(getPath(configFilePath));
  }

  private static String getPath(String configFilePath) {
    return Thread.currentThread().getContextClassLoader().getResource(configFilePath).getPath();
  }

  public TextAdapter getAdapter() {
    return adapter;
  }

  public TextAdapterConfig getConfig() {
    return config;
  }

  public ConceptQuery getQuery() {
    return query;
  }

  public TextFinder getFinder() {
    return new TextFinder(query, entities, adapter);
  }
}
