package care.smith.top.top_phenotypic_query.data_adapter.fhir;

import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapterSettings;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeQueryBuilder;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FHIRAdapterSettings extends DataAdapterSettings {

  private static FHIRAdapterSettings instance = null;

  private FHIRAdapterSettings() {}

  public static FHIRAdapterSettings get() {
    if (instance == null) instance = new FHIRAdapterSettings();
    return instance;
  }

  @Override
  public String formatNumber(BigDecimal num) {
    return num.toPlainString();
  }

  @Override
  public String formatDateTime(LocalDateTime date) {
    return DateUtil.format(date);
  }

  @Override
  public String formatBoolean(Boolean bool) {
    return bool.toString();
  }

  @Override
  public String formatString(String str) {
    return str;
  }

  @Override
  public String formatList(Stream<String> values) {
    return values.collect(Collectors.joining(","));
  }

  @Override
  public String formatOperator(RestrictionOperator oper) {
    if (oper == RestrictionOperator.GREATER_THAN) return "=gt";
    if (oper == RestrictionOperator.GREATER_THAN_OR_EQUAL_TO) return "=ge";
    if (oper == RestrictionOperator.LESS_THAN) return "=lt";
    return "=le";
  }

  @Override
  protected String getSexList(Restriction r, SubjectSearch search) {
    return Restrictions.getValuesAsString(r, this);
  }

  @Override
  protected Map<String, String> getBirthdateInterval(Restriction r, SubjectSearch search) {
    return Restrictions.getIntervalAsStringMap(r, this);
  }

  @Override
  protected void addCodeList(Phenotype p, PhenotypeQueryBuilder builder, SingleSearch search) {
    builder.baseQuery(getCodeUrisAsString(p));
  }

  @Override
  protected void addValueInterval(
      Restriction r, PhenotypeQueryBuilder builder, SingleSearch search) {
    Map<String, String> interval = Restrictions.getIntervalAsStringMap(r, this);
    for (String key : interval.keySet())
      builder.valueIntervalLimit(r.getType(), key, interval.get(key));
  }

  @Override
  protected void addValueList(Restriction r, PhenotypeQueryBuilder builder, SingleSearch search) {
    String valuesAsString = Restrictions.getValuesAsString(r, this);
    if (Restrictions.hasConceptType(r)) builder.conceptValueList(valuesAsString);
    else builder.valueList(r.getType(), valuesAsString);
  }

  @Override
  protected void addDateInterval(
      Restriction r, PhenotypeQueryBuilder builder, SingleSearch search) {
    Map<String, String> interval = Restrictions.getIntervalAsStringMap(r, this);
    for (String key : interval.keySet()) builder.dateTimeIntervalLimit(key, interval.get(key));
  }
}
