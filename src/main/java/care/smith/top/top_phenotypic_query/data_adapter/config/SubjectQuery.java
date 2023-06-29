package care.smith.top.top_phenotypic_query.data_adapter.config;

public class SubjectQuery {

  private String baseQuery;
  private String sexListPart;
  private String birthdateIntervalPart;
  private SubjectOutput output;

  public String getBaseQuery() {
    return baseQuery;
  }

  public void setBaseQuery(String baseQuery) {
    this.baseQuery = baseQuery;
  }

  public String getSexListPart() {
    return sexListPart;
  }

  public void setSexListPart(String sexListPart) {
    this.sexListPart = sexListPart;
  }

  public String getBirthdateIntervalPart() {
    return birthdateIntervalPart;
  }

  public void setBirthdateIntervalPart(String birthdateIntervalPart) {
    this.birthdateIntervalPart = birthdateIntervalPart;
  }

  public SubjectOutput getOutput() {
    return output;
  }

  public void setOutput(SubjectOutput output) {
    this.output = output;
  }

  public SubjectQueryBuilder getQueryBuilder() {
    return new SubjectQueryBuilder(this);
  }

  @Override
  public String toString() {
    return "SubjectQuery [baseQuery="
        + baseQuery
        + ", sexListPart="
        + sexListPart
        + ", birthdateIntervalPart="
        + birthdateIntervalPart
        + ", output="
        + output
        + "]";
  }
}
