package care.smith.top.top_phenotypic_query.result;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectPhenotypes extends LinkedHashMap<String, PhenotypeValues> {

  private static final long serialVersionUID = 1L;
  private String subjectId;

  private Logger log = LoggerFactory.getLogger(SubjectPhenotypes.class);

  public SubjectPhenotypes(String subjectId) {
    this.subjectId = subjectId;
  }

  public void setValues(PhenotypeValues values) {
    put(values.getPhenotypeName(), values);
  }

  public void setValues(PhenotypeValues... values) {
    for (PhenotypeValues vals : values) setValues(vals);
  }

  public void addValue(String phenotypeName, DateTimeRestriction dateRange, Value val) {
    PhenotypeValues values = get(phenotypeName);
    if (values == null) {
      values = new PhenotypeValues(phenotypeName);
      setValues(values);
    }
    values.addValue(dateRange, val);
    log.debug(
        "value is added: {}::{}::{}::{}",
        subjectId,
        phenotypeName,
        Restrictions.toString(dateRange),
        Values.toString(val));
  }

  public void setValues(String phenotypeName, DateTimeRestriction dateRange, List<Value> vals) {
    PhenotypeValues values = get(phenotypeName);
    if (values == null) {
      values = new PhenotypeValues(phenotypeName);
      setValues(values);
    }
    values.setValues(dateRange, vals);
    log.debug(
        "values added to result set: {}::{}::{}::{}",
        subjectId,
        phenotypeName,
        Restrictions.toString(dateRange),
        Values.toString(vals));
  }

  //  public void removeValues(String pheName, DateTimeRestriction dateRange) {
  //    PhenotypeValues vals = getValues(pheName);
  //    if (vals != null && vals.containsKey(dateRange)) {
  //      vals.remove(dateRange);
  //      log.debug(
  //          "values removed from result set: {}::{}::{}",
  //          subjectId,
  //          pheName,
  //          Restrictions.toString(dateRange));
  //    }
  //    if (vals != null && vals.isEmpty()) remove(pheName);
  //  }

  public PhenotypeValues getValues(String phenotypeName) {
    return get(phenotypeName);
  }

  public List<Value> getValues(String phenotypeName, DateTimeRestriction dateRange) {
    PhenotypeValues values = getValues(phenotypeName);
    if (values == null) return null;
    List<Value> list = values.getValues(dateRange);
    if (list != null) return list;
    return values.getValues(null);
  }

  public Set<String> getPhenotypeNames() {
    return keySet();
  }

  public boolean hasPhenotype(String phenotypeName) {
    return containsKey(phenotypeName);
  }

  public String getSubjectId() {
    return subjectId;
  }

  @Override
  public String toString() {
    StringBuffer sb =
        new StringBuffer("  Subject '")
            .append(subjectId)
            .append("':")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
    for (care.smith.top.top_phenotypic_query.result.PhenotypeValues values : values())
      sb.append(values).append(System.lineSeparator());
    return sb.toString();
  }
}
