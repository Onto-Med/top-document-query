package care.smith.top.top_phenotypic_query.data_adapter.fhir.resource_finder;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.data_adapter.config.SubjectOutput;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRPath;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRUtil;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.Resource;

public class FHIRPatientFinder extends FHIRPathResourceFinder {

  private SubjectSearch search;
  private SubjectOutput out;
  private Phenotype sex;
  private Phenotype bd;
  private Phenotype age;

  public FHIRPatientFinder(
      IGenericClient client, FHIRPath path, ResultSet rs, SubjectSearch search) {
    super(client, path, rs);
    this.search = search;
    this.out = search.getOutput();
    this.sex = search.getSex();
    this.bd = search.getBirthdateDerived();
    this.age = search.getAge();
  }

  @Override
  protected void addResource(Resource res) {
    String sbj = FHIRUtil.getId(res);
    if (bd == null && sex == null) {
      rs.addSubject(sbj);
      return;
    }
    if (bd != null) {
      LocalDateTime bdVal = path.getDateTime(res, out.getBirthdate());
      if (bdVal != null) {
        Value val = Val.of(bdVal);
        if (search.getBirthdate() != null) rs.addValueWithRestriction(sbj, bd, val);
        else rs.addValue(sbj, bd, null, val);
        if (age != null) {
          Value ageVal = Val.of(SubjectSearch.birthdateToAge(bdVal));
          rs.addValueWithRestriction(sbj, age, ageVal);
        }
      }
    }
    if (sex != null) {
      String sexVal = path.getString(res, out.getSex());
      if (sexVal != null) rs.addValueWithRestriction(sbj, sex, Val.of(sexVal));
    }
  }
}
