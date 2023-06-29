package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;

public class Pat extends Patient {

  private static final long serialVersionUID = 1L;

  public Pat() {}

  public Pat(String identifier) {
    addIdentifier().setSystem(Client.SYSTEM).setValue(identifier);
  }

  public Pat gender(AdministrativeGender gender) {
    setGender(gender);
    return this;
  }

  public Pat gender(String gender) {
    return gender(AdministrativeGender.fromCode(gender));
  }

  public Pat birthDate(LocalDateTime birthDate) {
    setBirthDate(DateUtil.toDate(birthDate));
    return this;
  }

  public Pat birthDate(String birthDate) {
    return birthDate(DateUtil.parse(birthDate));
  }
}
