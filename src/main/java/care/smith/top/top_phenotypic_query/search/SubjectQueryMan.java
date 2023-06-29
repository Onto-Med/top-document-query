package care.smith.top.top_phenotypic_query.search;

import care.smith.top.model.Phenotype;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SubjectQueryMan {

  private DataAdapter adapter;

  private Phenotype sexInclusion;
  private Phenotype birthdateInclusion;
  private Phenotype ageInclusion;

  private Phenotype sexExclusion;
  private Phenotype birthdateExclusion;
  private Phenotype ageExclusion;

  private Phenotype sexPhenotypeVariable;
  private Set<Phenotype> sexRestrictionVariables = new HashSet<>();
  private Phenotype birthdatePhenotypeVariable;
  private Set<Phenotype> birthdateRestrictionVariables = new HashSet<>();
  private Phenotype agePhenotypeVariable;
  private Set<Phenotype> ageRestrictionVariables = new HashSet<>();

  public SubjectQueryMan(DataAdapter adapter) {
    this.adapter = adapter;
  }

  public void setSexCriterion(QueryCriterion criterion, Phenotype phenotype) {
    if (criterion.isInclusion()) this.sexInclusion = phenotype;
    else this.sexExclusion = phenotype;
  }

  public void setBirthdateCriterion(QueryCriterion criterion, Phenotype phenotype) {
    if (criterion.isInclusion()) this.birthdateInclusion = phenotype;
    else this.birthdateExclusion = phenotype;
  }

  public void setAgeCriterion(QueryCriterion criterion, Phenotype phenotype) {
    if (criterion.isInclusion()) this.ageInclusion = phenotype;
    else this.ageExclusion = phenotype;
  }

  public void addSexVariable(Phenotype sexVariable) {
    if (Phenotypes.isPhenotype(sexVariable)) sexPhenotypeVariable = sexVariable;
    else if (Phenotypes.isRestriction(sexVariable)) {
      sexRestrictionVariables.add(sexVariable);
      sexPhenotypeVariable = sexVariable.getSuperPhenotype();
    }
  }

  public void addBirthdateVariable(Phenotype birthdateVariable) {
    if (Phenotypes.isPhenotype(birthdateVariable)) birthdatePhenotypeVariable = birthdateVariable;
    else if (Phenotypes.isRestriction(birthdateVariable)) {
      birthdateRestrictionVariables.add(birthdateVariable);
      birthdatePhenotypeVariable = birthdateVariable.getSuperPhenotype();
    }
  }

  public void addAgeVariable(Phenotype ageVariable) {
    if (Phenotypes.isPhenotype(ageVariable)) agePhenotypeVariable = ageVariable;
    else if (Phenotypes.isRestriction(ageVariable)) {
      ageRestrictionVariables.add(ageVariable);
      agePhenotypeVariable = ageVariable.getSuperPhenotype();
    }
  }

  public ResultSet executeInclusion() throws SQLException {
    if (!hasInclusion()) return null;
    Phenotype sex = getInclusion(sexInclusion, sexExclusion, sexPhenotypeVariable);
    Phenotype bd = getInclusion(birthdateInclusion, birthdateExclusion, birthdatePhenotypeVariable);
    Phenotype age = getInclusion(ageInclusion, ageExclusion, agePhenotypeVariable);
    return adapter.execute(new SubjectSearch(null, sex, bd, age, adapter));
  }

  private Phenotype getInclusion(Phenotype inc, Phenotype exc, Phenotype var) {
    if (inc != null) return inc;
    if (exc != null) return exc.getSuperPhenotype();
    return var;
  }

  public ResultSet executeSexExclusion() throws SQLException {
    if (sexExclusion == null) return null;
    return adapter.execute(new SubjectSearch(null, sexExclusion, null, null, adapter));
  }

  public ResultSet executeBirthdateExclusion() throws SQLException {
    if (birthdateExclusion == null) return null;
    return adapter.execute(new SubjectSearch(null, null, birthdateExclusion, null, adapter));
  }

  public ResultSet executeAgeExclusion() throws SQLException {
    if (ageExclusion == null) return null;
    return adapter.execute(
        new SubjectSearch(null, null, getBirthdateParameter(), ageExclusion, adapter));
  }

  public ResultSet executeAllSubjectsQuery() throws SQLException {
    return adapter.execute(
        new SubjectSearch(
            null, getSexParameter(), getBirthdateParameter(), getAgeParameter(), adapter));
  }

  public ResultSet executeBirthdateRestrictionVariable() throws SQLException {
    if (!hasBirthdateRestrictionVariable()) return new ResultSet();
    Phenotype bd = birthdateRestrictionVariables.iterator().next();
    if (hasBirthdateExclusion() && bd.getId().equals(birthdateExclusion.getId()))
      return new ResultSet();
    return adapter.execute(new SubjectSearch(null, null, bd, null, adapter));
  }

  public ResultSet executeAgeRestrictionVariable() throws SQLException {
    if (!hasAgeRestrictionVariable()) return new ResultSet();
    Phenotype age = ageRestrictionVariables.iterator().next();
    if (hasAgeExclusion() && age.getId().equals(ageExclusion.getId())) return new ResultSet();
    Phenotype bd = getBirthdateParameter();
    return adapter.execute(new SubjectSearch(null, null, bd, age, adapter));
  }

  public ResultSet executeSexRestrictionVariable() throws SQLException {
    if (!hasSexRestrictionVariable()) return new ResultSet();
    Phenotype sex = sexRestrictionVariables.iterator().next();
    if (hasSexExclusion() && sex.getId().equals(sexExclusion.getId())) return new ResultSet();
    return adapter.execute(new SubjectSearch(null, sex, null, null, adapter));
  }

  public boolean hasInclusion() {
    return hasParameter(sexInclusion, birthdateInclusion, ageInclusion);
  }

  private boolean hasExclusion() {
    return hasParameter(sexExclusion, birthdateExclusion, ageExclusion);
  }

  public boolean hasSexExclusion() {
    return sexExclusion != null;
  }

  public boolean hasBirthdateExclusion() {
    return birthdateExclusion != null;
  }

  public boolean hasAgeExclusion() {
    return ageExclusion != null;
  }

  public boolean hasVariables() {
    return hasParameter(sexPhenotypeVariable, birthdatePhenotypeVariable, agePhenotypeVariable);
  }

  public boolean hasSexRestrictionVariable() {
    return !sexRestrictionVariables.isEmpty();
  }

  public boolean hasBirthdateRestrictionVariable() {
    return !birthdateRestrictionVariables.isEmpty();
  }

  public boolean hasAgeRestrictionVariable() {
    return !ageRestrictionVariables.isEmpty();
  }

  private boolean hasParameter(Phenotype... phes) {
    for (Phenotype p : phes) if (p != null) return true;
    return false;
  }

  // no subject single IC but an unrestricted subject variable or
  // no subject single IC but more than one subject restriction of the same unrestricted phenotype
  // (age and birth date count together)
  public boolean hasComplexParameters() {
    int sexRestrNum = countRestrictions(sexExclusion, sexRestrictionVariables);
    int bdRestrNum = countRestrictions(birthdateExclusion, birthdateRestrictionVariables);
    int ageRestrNum = countRestrictions(ageExclusion, ageRestrictionVariables);

    return (!hasInclusion()
        && ((isUnrestricted(sexPhenotypeVariable, sexRestrictionVariables)
                || isUnrestricted(birthdatePhenotypeVariable, birthdateRestrictionVariables)
                || isUnrestricted(agePhenotypeVariable, ageRestrictionVariables))
            || sexRestrNum > 1
            || bdRestrNum + ageRestrNum > 1));
  }

  private boolean isUnrestricted(Phenotype var, Set<Phenotype> restrictions) {
    return var != null && restrictions.isEmpty();
  }

  private Phenotype getParameter(Phenotype... phes) {
    for (Phenotype p : phes) {
      if (p == null) continue;
      if (Phenotypes.isPhenotype(p)) return p;
      if (Phenotypes.isRestriction(p)) return p.getSuperPhenotype();
    }
    return null;
  }

  private Phenotype getSexParameter() {
    return getParameter(sexInclusion, sexExclusion, sexPhenotypeVariable);
  }

  private Phenotype getBirthdateParameter() {
    return getParameter(birthdateInclusion, birthdateExclusion, birthdatePhenotypeVariable);
  }

  private Phenotype getAgeParameter() {
    return getParameter(ageInclusion, ageExclusion, agePhenotypeVariable);
  }

  private int countRestrictions(Phenotype exc, Set<Phenotype> restrVars) {
    Set<String> restrictions = restrVars.stream().map(Phenotype::getId).collect(Collectors.toSet());
    if (exc != null) restrictions.add(exc.getId());
    return restrictions.size();
  }

  //  public ResultSet calculateInclusion(ResultSet main) {
  //    if (main == null || !hasInclusion()) return null;
  //
  //    Entities phes = getInclusionEntities();
  //    for (String sbjId : new HashSet<>(main.getSubjectIds())) {
  //      if (fulfillInclusion(phes, sbjId, sexInclusion, main))
  //        if (fulfillInclusion(phes, sbjId, birthdateInclusion, main))
  //          fulfillInclusion(phes, sbjId, ageInclusion, main);
  //    }
  //
  //    return main;
  //  }

  public ResultSet calculateExclusion(ResultSet main) {
    if (main == null || !hasExclusion()) return main;

    Entities phes = getExclusionEntities();
    for (String sbjId : new HashSet<>(main.getSubjectIds())) {
      if (!fulfillExclusion(phes, sbjId, sexExclusion, main))
        if (!fulfillExclusion(phes, sbjId, birthdateExclusion, main))
          fulfillExclusion(phes, sbjId, ageExclusion, main);
    }

    return main;
  }

  //  private boolean fulfillInclusion(Entities phes, String sbjId, Phenotype inc, ResultSet main) {
  //    if (inc == null) return true;
  //    boolean res = calculateCriterion(phes, main.getPhenotypes(sbjId), inc);
  //    if (!res) main.removeSubject(sbjId);
  //    return res;
  //  }

  private boolean fulfillExclusion(Entities phes, String sbjId, Phenotype exc, ResultSet main) {
    if (exc == null) return false;
    boolean res = calculateCriterion(phes, main.getPhenotypes(sbjId), exc);
    if (res) main.removeSubject(sbjId);
    return res;
  }

  private boolean calculateCriterion(Entities phes, SubjectPhenotypes sbjPhes, Phenotype cri) {
    return Expressions.getBooleanValue(new C2R().phenotypes(phes).values(sbjPhes).calculate(cri));
  }

  //  private Entities getInclusionEntities() {
  //    Entities e = Entities.of();
  //    if (sexInclusion != null) e.add(sexInclusion, sexInclusion.getSuperPhenotype());
  //    if (birthdateInclusion != null)
  //      e.add(birthdateInclusion, birthdateInclusion.getSuperPhenotype());
  //    if (ageInclusion != null) e.add(ageInclusion, ageInclusion.getSuperPhenotype());
  //    return e;
  //  }

  private Entities getExclusionEntities() {
    Entities e = Entities.of();
    if (sexExclusion != null) e.add(sexExclusion, sexExclusion.getSuperPhenotype());
    if (birthdateExclusion != null)
      e.add(birthdateExclusion, birthdateExclusion.getSuperPhenotype());
    if (ageExclusion != null) e.add(ageExclusion, ageExclusion.getSuperPhenotype());
    return e;
  }

  @Override
  public String toString() {
    return "SubjectQueryMan [sexInclusion="
        + sexInclusion
        + ", birthdateInclusion="
        + birthdateInclusion
        + ", ageInclusion="
        + ageInclusion
        + ", sexExclusion="
        + sexExclusion
        + ", birthdateExclusion="
        + birthdateExclusion
        + ", ageExclusion="
        + ageExclusion
        + ", sexPhenotypeVariable="
        + sexPhenotypeVariable
        + ", sexRestrictionVariables="
        + sexRestrictionVariables
        + ", birthdatePhenotypeVariable="
        + birthdatePhenotypeVariable
        + ", birthdateRestrictionVariables="
        + birthdateRestrictionVariables
        + ", agePhenotypeVariable="
        + agePhenotypeVariable
        + ", ageRestrictionVariables="
        + ageRestrictionVariables
        + "]";
  }
}
