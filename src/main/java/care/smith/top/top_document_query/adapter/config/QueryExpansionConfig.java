package care.smith.top.top_document_query.adapter.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collections;
import java.util.Map;

public class QueryExpansionConfig {
  private String profile;

  @JsonSetter(nulls = Nulls.SKIP)
  private Map<String, QueryExpansionRelationConfig> relations = Collections.emptyMap();

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public Map<String, QueryExpansionRelationConfig> getRelations() {
    return relations;
  }

  public void setRelations(Map<String, QueryExpansionRelationConfig> relations) {
    this.relations = relations;
  }
}
