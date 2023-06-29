package care.smith.top.top_phenotypic_query.result;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PhenotypeValues extends LinkedHashMap<DateTimeRestriction, List<Value>> {

  private static final long serialVersionUID = 1L;
  private String phenotypeName;

  public PhenotypeValues(String phenotypeName) {
    this.phenotypeName = phenotypeName;
  }

  public PhenotypeValues phenotypeName(String phenotypeName) {
    this.phenotypeName = phenotypeName;
    return this;
  }

  public String getPhenotypeName() {
    return phenotypeName;
  }

  public void setValues(DateTimeRestriction dateRange, List<Value> values) {
    put(dateRange, values);
  }

  public void addValue(DateTimeRestriction dateRange, Value value) {
    List<Value> vals = get(dateRange);

    if (vals == null) {
      vals = new ArrayList<>();
      put(dateRange, vals);
    }

    vals.add(value);
  }

  public void setValues(DateTimeRestriction dateRange, Value... values) {
    put(dateRange, List.of(values));
  }

  public List<Value> getValues(DateTimeRestriction dateRange) {
    return get(dateRange);
  }

  public boolean hasDateTimeRestriction(DateTimeRestriction dateRange) {
    return containsKey(dateRange);
  }

  public Set<DateTimeRestriction> getDateTimeRestrictions() {
    return keySet();
  }

  @Override
  public int hashCode() {
    return Objects.hash(phenotypeName, entrySet());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PhenotypeValues other = (PhenotypeValues) obj;
    return Objects.equals(phenotypeName, other.phenotypeName)
        && Objects.equals(entrySet(), other.entrySet());
  }

  @Override
  public String toString() {
    StringBuffer sb =
        new StringBuffer("    Phenotype '")
            .append(phenotypeName)
            .append("':")
            .append(System.lineSeparator());
    for (java.util.Map.Entry<DateTimeRestriction, List<Value>> entry : entrySet())
      sb.append("    ")
          .append(Restrictions.toString(entry.getKey()))
          .append("::")
          .append(Values.toString(entry.getValue()))
          .append(System.lineSeparator());
    return sb.toString();
  }
}
