package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.*;
import care.smith.top.model.ProjectionEntry.TypeEnum;
import care.smith.top.top_phenotypic_query.search.SingleQueryMan;
import care.smith.top.top_phenotypic_query.search.SubjectQueryMan;

public class Queries {

  public static QueryType getType(
      PhenotypeQuery query, Entities phenotypes, SubjectQueryMan sbjMan, SingleQueryMan sinMan) {
    boolean criExist = false;
    boolean sic = false;
    boolean cicWithoutNegation = false;

    if (query.getCriteria() != null)
      for (QueryCriterion cri : query.getCriteria()) {
        criExist = true;
        Phenotype phe = phenotypes.getPhenotype(cri.getSubjectId());
        if (cri.isInclusion()) {
          if (Phenotypes.isSingle(phe)) sic = true;
          else if ((Phenotypes.isCompositePhenotype(phe)
                  && !Expressions.containsNegation(phe.getExpression(), phenotypes))
              || (Phenotypes.isCompositeRestriction(phe)
                  && !Expressions.containsNegation(
                      phe.getSuperPhenotype().getExpression(), phenotypes)))
            cicWithoutNegation = true;
        }
      }

    if (criExist && (!sic && !cicWithoutNegation) || sbjMan.hasComplexParameters())
      return QueryType.TYPE_1;
    if (sbjMan.hasInclusion()) return QueryType.TYPE_2;
    if (sinMan.hasInclusion()) return QueryType.TYPE_3;
    return QueryType.TYPE_4;
  }

  public enum QueryType {
    TYPE_1, // all subjects query (ASQ) required, if criteria exist and:
    // a) no single IC and no composite IC without negation or
    // b) no subject single IC but an unrestricted subject variable or
    // c) no subject single IC but more than one subject restriction of the same unrestricted
    // phenotype (age and birth date count together)
    TYPE_2, // no ASQ, but subject single IC
    TYPE_3, // no ASQ, no subject single IC, but other single IC
    TYPE_4 // no ASQ, no single IC (i.e., composite IC without negation exist or no criteria exist
    // but only projection)
  }

  public static boolean isCriterion(ProjectionEntry entry) {
    return entry.getType() == TypeEnum.QUERYCRITERION;
  }
}
