package care.smith.top.top_phenotypic_query.data_adapter.config;

public class SubjectQueryBuilder extends QueryBuilder {

  private SubjectQuery query;

  public SubjectQueryBuilder(SubjectQuery query) {
    this.query = query;
  }

  public SubjectQueryBuilder baseQuery() {
    add(query.getBaseQuery());
    return this;
  }

  public SubjectQueryBuilder sexList(String sexes) {
    add(query.getSexListPart(), sexes);
    return this;
  }

  public SubjectQueryBuilder birthdateIntervalLimit(String operator, String birthdate) {
    add(query.getBirthdateIntervalPart(), operator, birthdate);
    return this;
  }
}
