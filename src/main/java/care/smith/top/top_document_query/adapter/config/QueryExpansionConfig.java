package care.smith.top.top_document_query.adapter.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collections;
import java.util.Map;

public class QueryExpansionConfig {
  private Boolean enabled;

  /** @deprecated Query-expansion profiles are selected by the caller and owned by Concept Graphs. */
  @Deprecated private String profile;

  @JsonSetter(nulls = Nulls.SKIP)
  private Map<String, QueryExpansionRelationConfig> relations = Collections.emptyMap();

  public boolean isEnabled() {
    return enabled != null ? enabled : profile != null;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /** @deprecated Query-expansion profiles are selected by the caller and owned by Concept Graphs. */
  @Deprecated
  public String getProfile() {
    return profile;
  }

  /** @deprecated Query-expansion profiles are selected by the caller and owned by Concept Graphs. */
  @Deprecated
  public void setProfile(String profile) {
    this.profile = profile;
  }

  /** @deprecated Relation strategy mapping is centralized in TOP. */
  @Deprecated
  public Map<String, QueryExpansionRelationConfig> getRelations() {
    return relations;
  }

  /** @deprecated Relation strategy mapping is centralized in TOP. */
  @Deprecated
  public void setRelations(Map<String, QueryExpansionRelationConfig> relations) {
    this.relations = relations;
  }
}
