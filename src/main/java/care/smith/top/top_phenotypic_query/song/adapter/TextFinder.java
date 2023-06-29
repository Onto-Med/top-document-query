package care.smith.top.top_phenotypic_query.song.adapter;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Entity;
import care.smith.top.top_phenotypic_query.util.Entities;
import java.util.List;

public class TextFinder {

  private ConceptQuery query;
  private Entities entities;
  private TextAdapter adapter;

  private TextAdapterConfig config;

  public TextFinder(ConceptQuery query, Entity[] entities, TextAdapter adapter) {
    this.query = query;
    this.adapter = adapter;
    this.config = adapter.getConfig();
    this.entities = Entities.of(entities);
  }

  public Entities getEntities() {
    return entities;
  }

  public List<Document> execute() {
    return adapter.execute(query, entities);
  }
}
