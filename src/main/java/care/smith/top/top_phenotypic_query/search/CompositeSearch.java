package care.smith.top.top_phenotypic_query.search;

import care.smith.top.model.Expression;
import care.smith.top.model.Phenotype;
import care.smith.top.model.ProjectionEntry;
import care.smith.top.model.Query;
import care.smith.top.model.QueryCriterion;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Queries;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeSearch extends PhenotypeSearch {

  private ProjectionEntry entry;
  private ResultSet rs;
  private Entities phenotypes;

  private Logger log = LoggerFactory.getLogger(CompositeSearch.class);

  public CompositeSearch(Query query, ProjectionEntry entry, ResultSet rs, Entities phenotypes) {
    super(query);
    this.entry = entry;
    this.rs = rs;
    this.phenotypes = phenotypes;
  }

  @Override
  public ResultSet execute() {
    Phenotype phe = phenotypes.getPhenotype(entry.getSubjectId());
    if (phe.getExpression() == null)
      throw new IllegalArgumentException(
          String.format(
              "A query criterion phenotype must have an expression! The phenotype '%s' has no"
                  + " expression.",
              phe.getId()));
    for (String sbjId : new HashSet<>(rs.getSubjectIds())) executeForSubject(phe, sbjId);
    return rs;
  }

  private void executeForSubject(Phenotype phe, String sbjId) {
    log.debug("start composite search: {} :: {} ...", sbjId, phe.getId());

    C2R c2r =
        new C2R()
            .phenotypes(phenotypes)
            .values(rs.getPhenotypes(sbjId))
            .dateTimeRestriction(entry.getDateTimeRestriction())
            .defaultAggregateFunction(entry.getDefaultAggregationFunctionId());

    Expression resExp = c2r.calculate(phe);

    log.debug("end composite search: {} :: {} :: {}", sbjId, phe.getId(), c2r.toString(resExp));

    if (Queries.isCriterion(entry)) {
      boolean res = (resExp == null) ? false : Expressions.getBooleanValue(resExp);
      QueryCriterion cri = (QueryCriterion) entry;
      if ((!cri.isInclusion() && res) || (cri.isInclusion() && !res)) rs.removeSubject(sbjId);
    }
  }
}
