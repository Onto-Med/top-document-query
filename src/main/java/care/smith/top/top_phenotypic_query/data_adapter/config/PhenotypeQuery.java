package care.smith.top.top_phenotypic_query.data_adapter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhenotypeQuery {

  private String baseQuery;
  private String dateTimeIntervalPart;
  private String subjectsPart;
  private String valueIntervalPart;
  private String valueListPart;
  private String numberValueIntervalPart;
  private String numberValueListPart;
  private String dateTimeValueIntervalPart;
  private String dateTimeValueListPart;
  private String textValueListPart;
  private String booleanValueListPart;
  private String conceptValueListPart;
  private PhenotypeOutput output;
  private List<String> union = new ArrayList<>();

  public String getBaseQuery() {
    return baseQuery;
  }

  public void setBaseQuery(String baseQuery) {
    this.baseQuery = baseQuery;
  }

  public String getDateTimeIntervalPart() {
    return dateTimeIntervalPart;
  }

  public void setDateTimeIntervalPart(String dateTimeIntervalPart) {
    this.dateTimeIntervalPart = dateTimeIntervalPart;
  }

  public String getSubjectsPart() {
    return subjectsPart;
  }

  public void setSubjectsPart(String subjectsPart) {
    this.subjectsPart = subjectsPart;
  }

  // Value

  public String getValueIntervalPart() {
    return valueIntervalPart;
  }

  public void setValueIntervalPart(String valueIntervalPart) {
    this.valueIntervalPart = valueIntervalPart;
  }

  public String getValueListPart() {
    return valueListPart;
  }

  public void setValueListPart(String valueListPart) {
    this.valueListPart = valueListPart;
  }

  // Number Value

  public String getNumberValueIntervalPart() {
    return numberValueIntervalPart;
  }

  public void setNumberValueIntervalPart(String numberValueIntervalPart) {
    this.numberValueIntervalPart = numberValueIntervalPart;
  }

  public String getNumberValueListPart() {
    return numberValueListPart;
  }

  public void setNumberValueListPart(String numberValueListPart) {
    this.numberValueListPart = numberValueListPart;
  }

  // Date Time Value

  public String getDateTimeValueIntervalPart() {
    return dateTimeValueIntervalPart;
  }

  public void setDateTimeValueIntervalPart(String dateTimeValueIntervalPart) {
    this.dateTimeValueIntervalPart = dateTimeValueIntervalPart;
  }

  public String getDateTimeValueListPart() {
    return dateTimeValueListPart;
  }

  public void setDateTimeValueListPart(String dateTimeValueListPart) {
    this.dateTimeValueListPart = dateTimeValueListPart;
  }

  // Text Value

  public void setTextValueListPart(String textValueListPart) {
    this.textValueListPart = textValueListPart;
  }

  public String getTextValueListPart() {
    return textValueListPart;
  }

  // Boolean Value

  public void setBooleanValueListPart(String booleanValueListPart) {
    this.booleanValueListPart = booleanValueListPart;
  }

  public String getBooleanValueListPart() {
    return booleanValueListPart;
  }

  // Concept Value

  public String getConceptValueListPart() {
    return conceptValueListPart;
  }

  public void setConceptValueListPart(String conceptValueListPart) {
    this.conceptValueListPart = conceptValueListPart;
  }

  public PhenotypeOutput getOutput() {
    return output;
  }

  public void setOutput(PhenotypeOutput output) {
    this.output = output;
  }

  public List<String> getUnion() {
    return union;
  }

  public void setUnion(List<String> union) {
    this.union = union;
  }

  public boolean hasUnion() {
    return !union.isEmpty();
  }

  public PhenotypeQueryBuilder getQueryBuilder(Map<String, String> mappings) {
    return new PhenotypeQueryBuilder(this, mappings);
  }

  @Override
  public String toString() {
    return "PhenotypeQuery [baseQuery="
        + baseQuery
        + ", dateTimeIntervalPart="
        + dateTimeIntervalPart
        + ", subjectsPart="
        + subjectsPart
        + ", valueIntervalPart="
        + valueIntervalPart
        + ", valueListPart="
        + valueListPart
        + ", numberValueIntervalPart="
        + numberValueIntervalPart
        + ", numberValueListPart="
        + numberValueListPart
        + ", dateTimeValueIntervalPart="
        + dateTimeValueIntervalPart
        + ", dateTimeValueListPart="
        + dateTimeValueListPart
        + ", textValueListPart="
        + textValueListPart
        + ", booleanValueListPart="
        + booleanValueListPart
        + ", conceptValueListPart="
        + conceptValueListPart
        + ", output="
        + output
        + ", union="
        + union
        + "]";
  }
}
