package care.smith.top.top_phenotypic_query.data_adapter.config;

import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeMapping {

  private String code;
  private String type;
  private String unit;
  private Map<String, String> phenotypeMappings = new HashMap<>();
  private Map<Restriction, Restriction> restrictionMappings = new HashMap<>();

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getPhenotypeMapping(String name) {
    return phenotypeMappings.get(name);
  }

  public Map<String, String> getPhenotypeMappings() {
    return phenotypeMappings;
  }

  public void setPhenotypeMappings(Map<String, String> phenotypeMappings) {
    this.phenotypeMappings = phenotypeMappings;
  }

  public void setRestrictionMappings(List<RestrictionMapping> restrictionMappings) {
    for (RestrictionMapping rm : restrictionMappings)
      this.restrictionMappings.put(rm.getModelRestriction(), rm.getSourceRestriction());
  }

  public boolean hasRestrictionMappings() {
    return !restrictionMappings.isEmpty();
  }

  public Restriction getSourceRestriction(Restriction modelRestriction) {
    return getSourceRestriction(modelRestriction, null);
  }

  public Restriction getSourceRestriction(Restriction modelRestriction, Phenotype phe) {
    Restriction sourceRestriction =
        restrictionMappings.get(Restrictions.copyWithoutBasicAttributes(modelRestriction));
    if (sourceRestriction == null) {
      if (unit == null) return modelRestriction;
      else return Restrictions.convertValues(modelRestriction, phe.getUnit(), unit);
    }
    return Restrictions.setType(
        sourceRestriction
            .cardinality(modelRestriction.getCardinality())
            .quantifier(modelRestriction.getQuantifier()));
  }

  @Override
  public String toString() {
    return "CodeMapping [code="
        + code
        + ", type="
        + type
        + ", unit="
        + unit
        + ", phenotypeMappings="
        + phenotypeMappings
        + ", restrictionMappings="
        + restrictionMappings
        + "]";
  }
}
