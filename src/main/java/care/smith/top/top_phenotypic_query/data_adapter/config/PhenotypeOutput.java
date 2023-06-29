package care.smith.top.top_phenotypic_query.data_adapter.config;

import care.smith.top.model.DataType;
import java.util.Map;

public class PhenotypeOutput {

  private String subject;
  private String dateTime;
  private String startDateTime;
  private String endDateTime;
  private String value;
  private String textValue;
  private String numberValue;
  private String dateTimeValue;
  private String booleanValue;
  private String conceptValue;

  private Map<String, String> mapping;

  public PhenotypeOutput mapping(Map<String, String> mapping) {
    this.mapping = mapping;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getDateTime() {
    if (dateTime == null || mapping == null) return dateTime;
    return QueryBuilder.replace(dateTime, mapping);
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  public String getStartDateTime() {
    if (startDateTime == null || mapping == null) return startDateTime;
    return QueryBuilder.replace(startDateTime, mapping);
  }

  public void setStartDateTime(String startDateTime) {
    this.startDateTime = startDateTime;
  }

  public String getEndDateTime() {
    if (endDateTime == null || mapping == null) return endDateTime;
    return QueryBuilder.replace(endDateTime, mapping);
  }

  public void setEndDateTime(String endDateTime) {
    this.endDateTime = endDateTime;
  }

  public String getValue(DataType dt) {
    String val = getValue();
    if (val != null) return val;
    if (dt == DataType.NUMBER) return getNumberValue();
    if (dt == DataType.STRING) return getTextValue();
    if (dt == DataType.DATE_TIME) return getDateTimeValue();
    if (dt == DataType.BOOLEAN) return getBooleanValue();
    return null;
  }

  public String getValue() {
    if (value == null) return null;
    if (mapping == null) return value;
    return QueryBuilder.replace(value, mapping);
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getTextValue() {
    if (textValue == null) return null;
    if (mapping == null) return textValue;
    return QueryBuilder.replace(textValue, mapping);
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getNumberValue() {
    if (numberValue == null) return null;
    if (mapping == null) return numberValue;
    return QueryBuilder.replace(numberValue, mapping);
  }

  public void setNumberValue(String numberValue) {
    this.numberValue = numberValue;
  }

  public String getDateTimeValue() {
    if (dateTimeValue == null) return null;
    if (mapping == null) return dateTimeValue;
    return QueryBuilder.replace(dateTimeValue, mapping);
  }

  public void setDateTimeValue(String dateTimeValue) {
    this.dateTimeValue = dateTimeValue;
  }

  public String getBooleanValue() {
    if (booleanValue == null) return null;
    if (mapping == null) return booleanValue;
    return QueryBuilder.replace(booleanValue, mapping);
  }

  public void setBooleanValue(String booleanValue) {
    this.booleanValue = booleanValue;
  }

  public String getConceptValue() {
    if (conceptValue == null) return null;
    if (mapping == null) return conceptValue;
    return QueryBuilder.replace(conceptValue, mapping);
  }

  public void setConceptValue(String conceptValue) {
    this.conceptValue = conceptValue;
  }

  @Override
  public String toString() {
    return "PhenotypeOutput [subject="
        + subject
        + ", dateTime="
        + dateTime
        + ", startDateTime="
        + startDateTime
        + ", endDateTime="
        + endDateTime
        + ", value="
        + value
        + ", textValue="
        + textValue
        + ", numberValue="
        + numberValue
        + ", dateTimeValue="
        + dateTimeValue
        + ", booleanValue="
        + booleanValue
        + ", conceptValue="
        + conceptValue
        + "]";
  }
}
