package care.smith.top.top_phenotypic_query.data_adapter.config;

import care.smith.top.model.DataType;
import java.util.Map;

public class PhenotypeQueryBuilder extends QueryBuilder {

  private PhenotypeQuery query;
  private Map<String, String> mappings;

  public PhenotypeQueryBuilder(PhenotypeQuery query, Map<String, String> mappings) {
    this.query = query;
    this.mappings = mappings;
  }

  public PhenotypeQueryBuilder baseQuery() {
    add(query.getBaseQuery(), mappings);
    return this;
  }

  public PhenotypeQueryBuilder baseQuery(String codes) {
    addCodes(query.getBaseQuery(), codes, mappings);
    return this;
  }

  public PhenotypeQueryBuilder valueIntervalLimit(DataType dt, String operator, String value) {
    if (query.getValueIntervalPart() != null) return valueIntervalLimit(operator, value);
    if (dt == DataType.NUMBER) return numberValueIntervalLimit(operator, value);
    if (dt == DataType.DATE_TIME) return dateTimeValueIntervalLimit(operator, value);
    return this;
  }

  public PhenotypeQueryBuilder valueList(DataType dt, String values) {
    if (query.getValueListPart() != null) return valueList(values);
    if (dt == DataType.NUMBER) return numberValueList(values);
    if (dt == DataType.DATE_TIME) return dateTimeValueList(values);
    if (dt == DataType.STRING) return textValueList(values);
    if (dt == DataType.BOOLEAN) return booleanValueList(values);
    return this;
  }

  public PhenotypeQueryBuilder valueIntervalLimit(String operator, String value) {
    add(query.getValueIntervalPart(), operator, value, mappings);
    return this;
  }

  public PhenotypeQueryBuilder valueList(String values) {
    add(query.getValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder numberValueIntervalLimit(String operator, String value) {
    add(query.getNumberValueIntervalPart(), operator, value, mappings);
    return this;
  }

  public PhenotypeQueryBuilder numberValueList(String values) {
    add(query.getNumberValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder dateTimeValueIntervalLimit(String operator, String value) {
    add(query.getDateTimeValueIntervalPart(), operator, value, mappings);
    return this;
  }

  public PhenotypeQueryBuilder dateTimeValueList(String values) {
    add(query.getDateTimeValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder textValueList(String values) {
    add(query.getTextValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder booleanValueList(String values) {
    add(query.getBooleanValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder conceptValueList(String values) {
    add(query.getConceptValueListPart(), values, mappings);
    return this;
  }

  public PhenotypeQueryBuilder dateTimeIntervalLimit(String operator, String value) {
    add(query.getDateTimeIntervalPart(), operator, value, mappings);
    return this;
  }

  public PhenotypeQueryBuilder subjects(String subjectIds) {
    add(query.getSubjectsPart(), subjectIds);
    return this;
  }
}
