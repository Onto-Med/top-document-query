package care.smith.top.top_phenotypic_query.data_adapter;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Quantifier;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeQueryBuilder;
import care.smith.top.top_phenotypic_query.data_adapter.config.Props;
import care.smith.top.top_phenotypic_query.data_adapter.config.SubjectQueryBuilder;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

public abstract class DataAdapterSettings {

  public String createSubjectPreparedQuery(SubjectSearch search) {
    SubjectQueryBuilder builder = search.getSubjectQuery().getQueryBuilder().baseQuery();

    if (search.hasSexRestriction()) {
      Restriction sexR = search.getSexRestriction();
      if (Restrictions.hasValues(sexR))
        builder.sexList(getSexList(search.getSexMapping().getSourceRestriction(sexR), search));
    }

    if (search.hasBirthdateRestriction()) {
      Restriction birthdateR = search.getBirthdateRestriction();
      if (Restrictions.hasInterval(birthdateR)) {
        Map<String, String> interval =
            getBirthdateInterval(
                search.getBirthdateMapping().getSourceRestriction(birthdateR), search);
        for (String key : interval.keySet()) builder.birthdateIntervalLimit(key, interval.get(key));
      }
    }

    return builder.build();
  }

  public String createSinglePreparedQuery(SingleSearch search) {
    PhenotypeQueryBuilder builder =
        search.getPhenotypeQuery().getQueryBuilder(search.getPhenotypeMappings());
    DateTimeRestriction dtr = search.getDateTimeRestriction();

    if (search.getPhenotypeQuery().getBaseQuery().contains(Props.VAR_CODES))
      addCodeList(search.getPhenotype(), builder, search);
    else builder.baseQuery();

    if (search.hasRestriction()) {
      Restriction r = search.getRestriction();
      if (r.getQuantifier() != Quantifier.ALL) {
        Restriction sourceR = search.getSourceRestriction();
        if (Restrictions.hasInterval(r)) addValueInterval(sourceR, builder, search);
        else if (Restrictions.hasValues(r)) addValueList(sourceR, builder, search);
      }
    }

    if (dtr != null) {
      if (Restrictions.hasInterval(dtr)) addDateInterval(dtr, builder, search);
    }

    return builder.build();
  }

  protected abstract String getSexList(Restriction r, SubjectSearch search);

  protected abstract Map<String, String> getBirthdateInterval(Restriction r, SubjectSearch search);

  protected abstract void addCodeList(
      Phenotype p, PhenotypeQueryBuilder builder, SingleSearch search);

  protected abstract void addValueInterval(
      Restriction r, PhenotypeQueryBuilder builder, SingleSearch search);

  protected abstract void addValueList(
      Restriction r, PhenotypeQueryBuilder builder, SingleSearch search);

  protected abstract void addDateInterval(
      Restriction r, PhenotypeQueryBuilder builder, SingleSearch search);

  public abstract String formatNumber(BigDecimal num);

  public abstract String formatDateTime(LocalDateTime date);

  public abstract String formatBoolean(Boolean bool);

  public abstract String formatString(String str);

  public abstract String formatList(Stream<String> values);

  public abstract String formatOperator(RestrictionOperator oper);

  public String getCodeUrisAsString(Phenotype p) {
    return formatList(Phenotypes.getUnrestrictedPhenotypeCodeUris(p).map(u -> formatString(u)));
  }
}
