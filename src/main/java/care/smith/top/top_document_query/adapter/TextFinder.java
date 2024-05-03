package care.smith.top.top_document_query.adapter;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.util.Entities;
import java.util.List;

public class TextFinder {

  private final ConceptQuery query;
  private final Entities entities;
  private final TextAdapter adapter;

  public TextFinder(ConceptQuery query, Entity[] entities, TextAdapter adapter) {
    this.query = query;
    this.adapter = adapter;
    this.entities = Entities.of(entities);
  }

  public Entities getEntities() {
    return entities;
  }

  public List<DocumentHit> execute() {
    return adapter.execute(query, entities);
  }
}
