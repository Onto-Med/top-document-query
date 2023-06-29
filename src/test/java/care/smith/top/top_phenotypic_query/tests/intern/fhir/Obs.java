package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

public class Obs extends Observation {

  private static final long serialVersionUID = 1L;

  private CodeableConcept cc = new CodeableConcept();

  public Obs() {}

  public Obs(String identifier, Reference patRef) {
    addIdentifier().setSystem(Client.SYSTEM).setValue(identifier);
    setSubject(patRef);
    setCode(cc);
  }

  public Obs code(String system, String code) {
    cc.addCoding(new Coding().setSystem(system).setCode(code));
    return this;
  }

  public Obs value(double value, String unit) {
    setValue(
        new Quantity()
            .setValue(value)
            .setUnit(unit)
            .setSystem("http://unitsofmeasure.org")
            .setCode(unit));
    return this;
  }

  public Obs value(double value) {
    setValue(new Quantity().setValue(value));
    return this;
  }

  public Obs date(LocalDateTime date) {
    setEffective(new DateTimeType(DateUtil.toDate(date)));
    return this;
  }

  public Obs date(String date) {
    return date(DateUtil.parse(date));
  }
}
