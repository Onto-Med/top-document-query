package care.smith.top.top_document_query.adapter;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.util.Entities;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextFinder {

  private final ConceptQuery query;
  private final Entities entities;
  private final TextAdapter adapter;
  private final Map<String, Entity> entityMap;
  private final Map<String, Set<String>> dependencyMap;

  public TextFinder(
      ConceptQuery query,
      Map<String, Entity> entityMap,
      Map<String, Set<String>> depenencyMap,
      TextAdapter adapter) {
    this.query = query;
    this.adapter = adapter;
    this.entityMap = entityMap;
    this.dependencyMap = depenencyMap;
    this.entities = Entities.of(entityMap.values().toArray(new Entity[0]));
  }

  public Entities getEntities() {
    return entities;
  }

  public Entity getEntity(String id) {
    return entityMap.get(id);
  }

  public Set<Entity> getChildrenOf(String id) {
    return dependencyMap.get(id).stream().map(entityMap::get).collect(Collectors.toSet());
  }

  public Set<String> getChildrenIdsOf(String id) {
    return dependencyMap.get(id);
  }

  public Stream<List<DocumentHit>> execute() {
    return adapter.execute(query, entityMap, dependencyMap);
  }
}
