package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.Category;
import care.smith.top.model.EntityType;

public class Categories {

  public static boolean isSingleConcept(Category cat) {
    return cat.getEntityType() == EntityType.SINGLE_CONCEPT;
  }

  public static boolean isCompositeConcept(Category cat) {
    return cat.getEntityType() == EntityType.COMPOSITE_CONCEPT;
  }
}
