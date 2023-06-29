package care.smith.top.top_phenotypic_query.tests.intern.fhir;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;

public class Med extends Medication {

  private static final long serialVersionUID = 1L;

  public Med() {}

  public Med(String identifier, String atcCode) {
    addIdentifier().setSystem(Client.SYSTEM).setValue(identifier);
    setCode(
        new CodeableConcept()
            .addCoding(new Coding().setSystem("http://www.whocc.no/atc").setCode(atcCode)));
  }
}
