package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.*;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Gt;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.search.SingleQueryMan;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectQueryMan;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Queries;
import care.smith.top.top_phenotypic_query.util.Queries.QueryType;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import org.junit.jupiter.api.Test;

public class SingleQueryTypeTest {

  private static final String CONFIG = "config/FHIR_Adapter_Test.yml";

  private static final Phenotype sex = new Phe("sex", "http://loinc.org", "46098-0").string().get();
  private static final Phenotype male =
      new Phe("male", "http://hl7.org/fhir/administrative-gender", "male")
          .restriction(sex, null)
          .get();
  private static final Phenotype female =
      new Phe("female", "http://hl7.org/fhir/administrative-gender", "female")
          .restriction(sex, null)
          .get();
  private static final Phenotype age = new Phe("age", "http://loinc.org", "30525-0").number().get();
  private static final Phenotype old = new Phe("old").restriction(age, Res.gt(80)).get();
  private static final Phenotype young = new Phe("young").restriction(age, Res.lt(20)).get();
  private static final Phenotype birthDate =
      new Phe("birthDate", "http://loinc.org", "21112-8").dateTime().get();
  private static final Phenotype birthDateYoung =
      new Phe("birthDateYoung").restriction(birthDate, Res.gt(DateUtil.parse("2000-01-01"))).get();
  private static final Phenotype birthDateOld =
      new Phe("birthDateOld").restriction(birthDate, Res.lt(DateUtil.parse("1950-01-01"))).get();
  private static final Phenotype weight =
      new Phe("weight", "http://loinc.org", "3141-9").number("kg").get();
  private static final Phenotype heavy = new Phe("heavy").restriction(weight, Res.gt(100)).get();
  private static final Phenotype height =
      new Phe("height", "http://loinc.org", "3137-7").number("cm").get();
  private static final Phenotype high = new Phe("high").restriction(height, Res.gt(200)).get();
  private static final Phenotype heavyAndHigh =
      new Phe("heavyAndHigh").expression(And.of(Exp.of(heavy), Exp.of(high))).get();
  private static final Phenotype heavyAndNotHigh =
      new Phe("heavyAndNotHigh").expression(And.of(Exp.of(heavy), Not.of(Exp.of(high)))).get();
  private static final Phenotype youngAndFemale =
      new Phe("youngAndFemale").expression(And.of(Exp.of(young), Exp.of(female))).get();
  private static final Phenotype youngAndNotFemale =
      new Phe("youngAndNotFemale").expression(And.of(Exp.of(young), Not.of(Exp.of(female)))).get();
  private static final Phenotype ageGt80 =
      new Phe("ageGt80").expression(Gt.of(Exp.of(age), Exp.of(80))).get();

  private static Entity[] phenotypes = {
    sex,
    male,
    female,
    age,
    old,
    young,
    birthDate,
    birthDateOld,
    birthDateYoung,
    weight,
    heavy,
    height,
    high,
    heavyAndHigh,
    heavyAndNotHigh,
    youngAndFemale,
    youngAndNotFemale,
    ageGt80
  };

  @Test
  public void test1() throws InstantiationException {
    Que q =
        new Que(CONFIG, phenotypes)
            .exc(high)
            .exc(female)
            .inc(heavyAndNotHigh)
            .inc(youngAndNotFemale);
    assertEquals(QueryType.TYPE_1, getType(q));

    q = new Que(CONFIG, phenotypes).inc(heavy).inc(ageGt80).exc(high).inc(heavyAndHigh);
    assertEquals(QueryType.TYPE_1, getType(q));

    q = new Que(CONFIG, phenotypes).inc(heavy).exc(youngAndNotFemale).exc(old);
    assertEquals(QueryType.TYPE_1, getType(q));
  }

  @Test
  public void test2() throws InstantiationException {
    Que q =
        new Que(CONFIG, phenotypes)
            .inc(old)
            .exc(high)
            .exc(female)
            .inc(heavyAndNotHigh)
            .inc(youngAndNotFemale);
    assertEquals(QueryType.TYPE_2, getType(q));

    q = new Que(CONFIG, phenotypes).inc(old).inc(heavy).inc(ageGt80).exc(high).inc(heavyAndHigh);
    assertEquals(QueryType.TYPE_2, getType(q));

    q = new Que(CONFIG, phenotypes).inc(old).inc(heavy).exc(youngAndNotFemale).exc(old);
    assertEquals(QueryType.TYPE_2, getType(q));
  }

  @Test
  public void test3() throws InstantiationException {
    Que q = new Que(CONFIG, phenotypes).inc(high).exc(female).inc(youngAndNotFemale);
    assertEquals(QueryType.TYPE_3, getType(q));
  }

  @Test
  public void test4() throws InstantiationException {
    Que q = new Que(CONFIG, phenotypes).exc(old).exc(female).inc(heavyAndHigh);
    assertEquals(QueryType.TYPE_4, getType(q));
  }

  private static QueryType getType(Que q) {
    DataAdapter adapter = q.getAdapter();
    PhenotypeQuery query = q.getQuery();
    DataAdapterConfig config = q.getConfig();
    Entities entities = q.getFinder().getPhenotypes();

    SubjectQueryMan sbjMan = new SubjectQueryMan(adapter);
    SingleQueryMan man = new SingleQueryMan(sbjMan, query, entities);
    for (QueryCriterion cri : query.getCriteria()) {
      Phenotype phe = entities.getPhenotype(cri.getSubjectId());
      if (Phenotypes.isSingle(phe)) {
        if (config.isAge(phe)) sbjMan.setAgeCriterion(cri, phe);
        else if (config.isBirthdate(phe)) sbjMan.setBirthdateCriterion(cri, phe);
        else if (config.isSex(phe)) sbjMan.setSexCriterion(cri, phe);
        else if (cri.isInclusion()) man.addInclusion(new SingleSearch(query, cri, phe, adapter));
        else man.addExclusion(new SingleSearch(query, cri, phe, adapter));
      }
    }

    for (QueryCriterion cri : query.getCriteria()) {
      Phenotype phe = entities.getPhenotype(cri.getSubjectId());
      if (Phenotypes.isComposite(phe)) {
        for (String var : Expressions.getVariables(phe.getExpression(), entities)) {
          Phenotype varPhe = entities.getPhenotype(var);
          if (Phenotypes.isSingle(varPhe)) {
            if (config.isAge(varPhe)) sbjMan.addAgeVariable(varPhe);
            else if (config.isBirthdate(varPhe)) sbjMan.addBirthdateVariable(varPhe);
            else if (config.isSex(varPhe)) sbjMan.addSexVariable(varPhe);
            else man.addVariable(new SingleSearch(query, cri, varPhe, adapter));
          }
        }
      }
    }

    return Queries.getType(query, entities, sbjMan, man);
  }
}
