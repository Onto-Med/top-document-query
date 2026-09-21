package care.smith.top.top_document_query.concept_graphs_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

public class QueryExpansionProfileEntity {
  private String name;

  @JsonProperty("language_name")
  private String languageName;

  private List<QueryExpansionProfileCategoryEntity> categories = Collections.emptyList();

  @JsonProperty("default_categories")
  private List<String> defaultCategories = Collections.emptyList();

  @JsonProperty("default_relations")
  private List<String> defaultRelations = Collections.emptyList();

  private List<QueryExpansionProfileRelationEntity> relations = Collections.emptyList();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLanguageName() {
    return languageName;
  }

  public void setLanguageName(String languageName) {
    this.languageName = languageName;
  }

  public List<QueryExpansionProfileCategoryEntity> getCategories() {
    return categories == null ? Collections.emptyList() : categories;
  }

  public void setCategories(List<QueryExpansionProfileCategoryEntity> categories) {
    this.categories = categories;
  }

  public List<String> getDefaultCategories() {
    return defaultCategories == null ? Collections.emptyList() : defaultCategories;
  }

  public void setDefaultCategories(List<String> defaultCategories) {
    this.defaultCategories = defaultCategories;
  }

  public List<String> getDefaultRelations() {
    return defaultRelations == null ? Collections.emptyList() : defaultRelations;
  }

  public void setDefaultRelations(List<String> defaultRelations) {
    this.defaultRelations = defaultRelations;
  }

  public List<QueryExpansionProfileRelationEntity> getRelations() {
    return relations == null ? Collections.emptyList() : relations;
  }

  public void setRelations(List<QueryExpansionProfileRelationEntity> relations) {
    this.relations = relations;
  }
}
