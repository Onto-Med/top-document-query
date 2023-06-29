package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Reference;

public class MedSta extends MedicationStatement {

  private static final long serialVersionUID = 1L;

  public MedSta() {}

  public MedSta(String identifier, Reference patRef, Reference medRef) {
    addIdentifier().setSystem(Client.SYSTEM).setValue(identifier);
    setSubject(patRef);
    setMedication(medRef);
  }

  public MedSta date(LocalDateTime date) {
    setEffective(new DateTimeType(DateUtil.toDate(date)));
    return this;
  }

  public MedSta date(String date) {
    return date(DateUtil.parse(date));
  }
}
