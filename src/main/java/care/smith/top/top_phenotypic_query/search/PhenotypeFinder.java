package care.smith.top.top_phenotypic_query.search;

import care.smith.top.model.Entity;
import care.smith.top.model.Phenotype;
import care.smith.top.model.PhenotypeQuery;
import care.smith.top.model.ProjectionEntry;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import java.sql.SQLException;

public class PhenotypeFinder {

  private PhenotypeQuery query;
  private Entities phenotypes;
  private DataAdapter adapter;
  private DataAdapterConfig config;

  public PhenotypeFinder(PhenotypeQuery query, Entity[] entities, DataAdapter adapter) {
    this.query = query;
    this.adapter = adapter;
    this.config = adapter.getConfig();
    this.phenotypes = Entities.of(entities).deriveAdditionalProperties(config);
  }

  public PhenotypeFinder(PhenotypeQuery query, Entities entities, DataAdapter adapter) {
    this.query = query;
    this.adapter = adapter;
    this.config = adapter.getConfig();
    this.phenotypes = entities.deriveAdditionalProperties(config);
  }

  public Entities getPhenotypes() {
    return phenotypes;
  }

  public ResultSet execute() throws SQLException {
    return executeCompositeSearches(executeSingleSearches()).clean(query.getProjection());
  }

  private ResultSet executeCompositeSearches(ResultSet rs) {
    if (query.getCriteria() != null)
      for (QueryCriterion cri : query.getCriteria()) executeCompositeSearch(cri, rs);

    if (query.getProjection() != null)
      for (ProjectionEntry pro : query.getProjection()) executeCompositeSearch(pro, rs);

    return rs;
  }

  private void executeCompositeSearch(ProjectionEntry pro, ResultSet rs) {
    Phenotype phe = phenotypes.getPhenotype(pro.getSubjectId());
    if (Phenotypes.isComposite(phe)) new CompositeSearch(query, pro, rs, phenotypes).execute();
  }

  private ResultSet executeSingleSearches() throws SQLException {
    SubjectQueryMan sbjMan = new SubjectQueryMan(adapter);
    SingleQueryMan man = new SingleQueryMan(sbjMan, query, phenotypes);

    if (query.getCriteria() != null) {
      for (QueryCriterion cri : query.getCriteria()) {
        Phenotype phe = phenotypes.getPhenotype(cri.getSubjectId());
        if (Phenotypes.isSingle(phe)) {
          if (config.isAge(phe)) sbjMan.setAgeCriterion(cri, phe);
          else if (config.isBirthdate(phe)) sbjMan.setBirthdateCriterion(cri, phe);
          else if (config.isSex(phe)) sbjMan.setSexCriterion(cri, phe);
          else if (cri.isInclusion()) man.addInclusion(new SingleSearch(query, cri, phe, adapter));
          else man.addExclusion(new SingleSearch(query, cri, phe, adapter));
        }
      }
      for (QueryCriterion cri : query.getCriteria()) {
        Phenotype phe = phenotypes.getPhenotype(cri.getSubjectId());
        if (Phenotypes.isComposite(phe)) {
          for (String var : Expressions.getVariables(phe.getExpression(), phenotypes))
            addVariable(var, cri, sbjMan, man);
        }
      }
    }

    if (query.getProjection() != null) {
      for (ProjectionEntry pro : query.getProjection())
        addVariable(pro.getSubjectId(), pro, sbjMan, man);
      for (ProjectionEntry pro : query.getProjection()) {
        Phenotype phe = phenotypes.getPhenotype(pro.getSubjectId());
        if (Phenotypes.isComposite(phe)) {
          for (String var : Expressions.getVariables(phe.getExpression(), phenotypes))
            addVariable(var, pro, sbjMan, man);
        }
      }
    }

    return man.execute();
  }

  private void addVariable(
      String var, ProjectionEntry pro, SubjectQueryMan sbjMan, SingleQueryMan man) {
    Phenotype varPhe = phenotypes.getPhenotype(var);
    if (Phenotypes.isSingle(varPhe)) {
      if (config.isAge(varPhe)) sbjMan.addAgeVariable(varPhe);
      else if (config.isBirthdate(varPhe)) sbjMan.addBirthdateVariable(varPhe);
      else if (config.isSex(varPhe)) sbjMan.addSexVariable(varPhe);
      else man.addVariable(new SingleSearch(query, pro, varPhe, adapter));
    }
  }
}
