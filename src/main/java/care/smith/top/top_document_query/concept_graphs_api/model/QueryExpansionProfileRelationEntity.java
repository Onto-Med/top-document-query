package care.smith.top.top_document_query.concept_graphs_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

public class QueryExpansionProfileRelationEntity {
  private String id;
  private String label;
  private String description;

  @JsonProperty("source_categories")
  private List<String> sourceCategories = Collections.emptyList();

  @JsonProperty("target_categories")
  private List<String> targetCategories = Collections.emptyList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getSourceCategories() {
    return sourceCategories == null ? Collections.emptyList() : sourceCategories;
  }

  public void setSourceCategories(List<String> sourceCategories) {
    this.sourceCategories = sourceCategories;
  }

  public List<String> getTargetCategories() {
    return targetCategories == null ? Collections.emptyList() : targetCategories;
  }

  public void setTargetCategories(List<String> targetCategories) {
    this.targetCategories = targetCategories;
  }
}
