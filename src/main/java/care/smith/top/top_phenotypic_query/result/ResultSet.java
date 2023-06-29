package care.smith.top.top_phenotypic_query.result;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Phenotype;
import care.smith.top.model.ProjectionEntry;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.ucum.UCUM;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSet extends LinkedHashMap<String, SubjectPhenotypes> {

  private static final long serialVersionUID = 1L;

  private Logger log = LoggerFactory.getLogger(ResultSet.class);

  public ResultSet() {}

  private ResultSet(ResultSet rs2) {
    super(rs2);
  }

  public void setPhenotypes(SubjectPhenotypes phenotypes) {
    put(phenotypes.getSubjectId(), phenotypes);
  }

  public void setPhenotypes(SubjectPhenotypes... phenotypes) {
    for (SubjectPhenotypes p : phenotypes) setPhenotypes(p);
  }

  public Set<String> getSubjectIds() {
    return keySet();
  }

  public Collection<SubjectPhenotypes> getPhenotypes() {
    return values();
  }

  public SubjectPhenotypes getPhenotypes(String subjectId) {
    return get(subjectId);
  }

  public void addValue(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange, Value val) {
    SubjectPhenotypes phes = get(subjectId);
    if (phes == null) {
      phes = new SubjectPhenotypes(subjectId);
      setPhenotypes(phes);
    }
    phes.addValue(phenotypeName, dateRange, val);
  }

  public void addValue(
      String subjectId, Phenotype phenotype, DateTimeRestriction dateRange, Value val) {
    if (val != null)
      addValue(subjectId, Phenotypes.getUnrestrictedPhenotypeId(phenotype), dateRange, val);
  }

  private Value convert(Value val, String sourceUnit, String modelUnit) {
    if (sourceUnit != null && modelUnit != null && Values.hasNumberType(val))
      return Val.of(
          UCUM.convert(Values.getNumberValue(val), sourceUnit, modelUnit), val.getDateTime());
    return val;
  }

  private void addRestriction(
      String subjectId, Phenotype phenotype, DateTimeRestriction dateRange, Value val) {
    if (Phenotypes.isRestriction(phenotype) && val != null) {
      List<Value> vals = getValues(subjectId, phenotype.getId(), dateRange);
      if (vals == null || vals.isEmpty())
        addValue(subjectId, phenotype.getId(), dateRange, Val.ofTrue());
    }
  }

  public void addValueWithRestriction(String subjectId, Phenotype phenotype, Value val) {
    addValue(subjectId, phenotype, null, val);
    addRestriction(subjectId, phenotype, null, val);
  }

  public void addValueWithRestriction(
      String subjectId,
      Phenotype phenotype,
      DateTimeRestriction dateRange,
      Value val,
      String sourceUnit,
      String modelUnit) {
    addValue(subjectId, phenotype, dateRange, convert(val, sourceUnit, modelUnit));
    addRestriction(subjectId, phenotype, dateRange, val);
  }

  public void replacePhenotype(String sbjId, String oldPheName, String newPheName) {
    SubjectPhenotypes phes = getPhenotypes(sbjId);
    PhenotypeValues vals = phes.remove(oldPheName).phenotypeName(newPheName);
    phes.setValues(vals);
  }

  //  public void removeValues(String sbjId, String pheName, DateTimeRestriction dateRange) {
  //    getPhenotypes(sbjId).removeValues(pheName, dateRange);
  //    SubjectPhenotypes phes = getPhenotypes(sbjId);
  //    if (phes != null && phes.isEmpty()) removeSubject(sbjId);
  //  }

  public ResultSet clean(List<ProjectionEntry> pro) {
    if (pro == null || pro.isEmpty()) return this;
    for (SubjectPhenotypes sbjPhens : getPhenotypes()) {
      SubjectPhenotypes newSbjPhens = new SubjectPhenotypes(sbjPhens.getSubjectId());
      for (ProjectionEntry pe : pro) {
        if (!addProjection(sbjPhens, newSbjPhens, pe.getSubjectId(), pe.getDateTimeRestriction())) {
          for (String pheName : sbjPhens.getPhenotypeNames()) {
            if (Phenotypes.isRestrictedValuesKeyOfPhenotype(pheName, pe.getSubjectId()))
              addProjection(sbjPhens, newSbjPhens, pheName, pe.getDateTimeRestriction());
          }
        }
      }
      setPhenotypes(newSbjPhens);
    }
    return this;
  }

  private boolean addProjection(
      SubjectPhenotypes sbjPhens,
      SubjectPhenotypes newSbjPhens,
      String pheName,
      DateTimeRestriction dtr) {
    List<Value> newVals = sbjPhens.getValues(pheName, dtr);
    if (newVals == null) return false;
    newSbjPhens.setValues(pheName, dtr, newVals);
    return true;
  }

  public void addSubject(String subjectId) {
    if (!getSubjectIds().contains(subjectId)) put(subjectId, new SubjectPhenotypes(subjectId));
  }

  public PhenotypeValues getValues(String subjectId, String phenotypeName) {
    SubjectPhenotypes subPhens = get(subjectId);
    if (subPhens == null) return null;
    return subPhens.getValues(phenotypeName);
  }

  public List<Value> getValues(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    PhenotypeValues values = getValues(subjectId, phenotypeName);
    if (values == null) return null;
    return values.getValues(dateRange);
  }

  public List<BigDecimal> getNumberValues(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<Value> vals = getValues(subjectId, phenotypeName, dateRange);
    if (vals == null || vals.isEmpty()) return new ArrayList<>();
    return Values.getNumberValues(vals);
  }

  public List<Boolean> getBooleanValues(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<Value> vals = getValues(subjectId, phenotypeName, dateRange);
    if (vals == null || vals.isEmpty()) return new ArrayList<>();
    return Values.getBooleanValues(vals);
  }

  public List<String> getStringValues(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<Value> vals = getValues(subjectId, phenotypeName, dateRange);
    if (vals == null || vals.isEmpty()) return new ArrayList<>();
    return Values.getStringValues(vals);
  }

  public BigDecimal getNumberValue(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<BigDecimal> vals = getNumberValues(subjectId, phenotypeName, dateRange);
    if (vals.isEmpty()) return null;
    return vals.get(0);
  }

  public Boolean getBooleanValue(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<Boolean> vals = getBooleanValues(subjectId, phenotypeName, dateRange);
    if (vals.isEmpty()) return null;
    return vals.get(0);
  }

  public String getStringValue(
      String subjectId, String phenotypeName, DateTimeRestriction dateRange) {
    List<String> vals = getStringValues(subjectId, phenotypeName, dateRange);
    if (vals.isEmpty()) return null;
    return vals.get(0);
  }

  public ResultSet intersect(ResultSet rs2) {
    ResultSet intersection = new ResultSet(this);
    intersection.keySet().retainAll(rs2.keySet());

    for (String sbjId : intersection.getSubjectIds()) {
      SubjectPhenotypes phenotypes1 = intersection.getPhenotypes(sbjId);
      SubjectPhenotypes phenotypes2 = rs2.getPhenotypes(sbjId);
      for (String pheName : phenotypes2.getPhenotypeNames()) {
        if (!phenotypes1.hasPhenotype(pheName))
          phenotypes1.setValues(phenotypes2.getValues(pheName));
        else {
          PhenotypeValues values1 = phenotypes1.getValues(pheName);
          PhenotypeValues values2 = phenotypes2.getValues(pheName);
          for (DateTimeRestriction dateRange : values2.getDateTimeRestrictions()) {
            if (!values1.hasDateTimeRestriction(dateRange))
              values1.setValues(dateRange, values2.getValues(dateRange));
          }
        }
      }
    }

    return intersection;
  }

  public ResultSet subtract(ResultSet rs2) {
    ResultSet difference = new ResultSet(this);
    difference.keySet().removeAll(rs2.keySet());
    return difference;
  }

  public ResultSet insert(ResultSet rs2) {
    ResultSet insert = new ResultSet(this);

    for (String sbjId : insert.getSubjectIds()) {
      SubjectPhenotypes phenotypes1 = insert.getPhenotypes(sbjId);
      SubjectPhenotypes phenotypes2 = rs2.getPhenotypes(sbjId);
      if (phenotypes2 == null) continue;
      for (String pheName : phenotypes2.getPhenotypeNames()) {
        if (!phenotypes1.hasPhenotype(pheName))
          phenotypes1.setValues(phenotypes2.getValues(pheName));
        else {
          PhenotypeValues values1 = phenotypes1.getValues(pheName);
          PhenotypeValues values2 = phenotypes2.getValues(pheName);
          for (DateTimeRestriction dateRange : values2.getDateTimeRestrictions()) {
            if (!values1.hasDateTimeRestriction(dateRange))
              values1.setValues(dateRange, values2.getValues(dateRange));
          }
        }
      }
    }

    return insert;
  }

  public ResultSet unite(ResultSet rs2) {
    ResultSet union = new ResultSet(this);

    for (String sbjId : rs2.getSubjectIds()) {
      SubjectPhenotypes phenotypes1 = union.getPhenotypes(sbjId);
      SubjectPhenotypes phenotypes2 = rs2.getPhenotypes(sbjId);
      if (phenotypes1 == null) {
        union.setPhenotypes(phenotypes2);
        continue;
      }
      for (String pheName : phenotypes2.getPhenotypeNames()) {
        if (!phenotypes1.hasPhenotype(pheName))
          phenotypes1.setValues(phenotypes2.getValues(pheName));
        else {
          PhenotypeValues values1 = phenotypes1.getValues(pheName);
          PhenotypeValues values2 = phenotypes2.getValues(pheName);
          for (DateTimeRestriction dateRange : values2.getDateTimeRestrictions()) {
            if (!values1.hasDateTimeRestriction(dateRange))
              values1.setValues(dateRange, values2.getValues(dateRange));
          }
        }
      }
    }

    return union;
  }

  public void removeSubject(String id) {
    remove(id);
    log.debug("subject removed from result set: {}", id);
  }

  @Override
  public String toString() {
    StringBuffer sb =
        new StringBuffer("Result Set:")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
    for (SubjectPhenotypes phes : values()) sb.append(phes);
    return sb.toString();
  }

  public String toString(Entities phenotypes) {
    String str = toString();
    for (Phenotype p : phenotypes.getPhenotypes())
      str = str.replaceAll(p.getId(), p.getId() + "::" + Entities.getFirstTitle(p));
    return str;
  }
}
