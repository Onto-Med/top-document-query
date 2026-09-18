package care.smith.top.top_document_query.concept_graphs_api.model;

import java.util.Collections;
import java.util.List;

public class QueryExpansionProfileEntity {
  private String name;
  private List<QueryExpansionProfileRelationEntity> relations = Collections.emptyList();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<QueryExpansionProfileRelationEntity> getRelations() {
    return relations == null ? Collections.emptyList() : relations;
  }

  public void setRelations(List<QueryExpansionProfileRelationEntity> relations) {
    this.relations = relations;
  }
}
