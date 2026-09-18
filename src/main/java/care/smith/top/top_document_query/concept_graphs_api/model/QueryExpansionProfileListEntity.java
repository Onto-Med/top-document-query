package care.smith.top.top_document_query.concept_graphs_api.model;

import java.util.Collections;
import java.util.List;

public class QueryExpansionProfileListEntity {
  private List<QueryExpansionProfileEntity> profiles = Collections.emptyList();

  public List<QueryExpansionProfileEntity> getProfiles() {
    return profiles == null ? Collections.emptyList() : profiles;
  }

  public void setProfiles(List<QueryExpansionProfileEntity> profiles) {
    this.profiles = profiles;
  }
}
