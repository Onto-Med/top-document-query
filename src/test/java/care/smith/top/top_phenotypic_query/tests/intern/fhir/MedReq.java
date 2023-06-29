package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;

public class MedReq extends MedicationRequest {

  private static final long serialVersionUID = 1L;

  public MedReq() {}

  public MedReq(String identifier, Reference patRef, Reference medRef) {
    addIdentifier().setSystem(Client.SYSTEM).setValue(identifier);
    setSubject(patRef);
    setMedication(medRef);
  }

  public MedReq date(LocalDateTime date) {
    setAuthoredOnElement(new DateTimeType(DateUtil.toDate(date)));
    return this;
  }

  public MedReq date(String date) {
    return date(DateUtil.parse(date));
  }
}
