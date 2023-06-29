package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRPath;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRUtil;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

public class FHIRPathTest {

  FHIRPath path = new FHIRPath(FhirContext.forR4());

  @Test
  void testPatient() {
    Patient patient = new Patient();
    patient.addIdentifier().setSystem("http://acme.org/mrns").setValue("12345");
    patient.setGender(Enumerations.AdministrativeGender.MALE);
    patient.setBirthDate(DateUtil.parseToDate("2001-02-03T04:05"));

    assertEquals("http://acme.org/mrns", path.getString(patient, "identifier.system.value"));
    assertEquals("male", path.getString(patient, "gender"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(patient, "birthDate"));
  }

  @Test
  void testObservation1() {
    Observation obs =
        new Observation()
            .setSubject(new Reference("Patient/1"))
            .setEffective(FHIRUtil.parse("2001-02-03T04:05"))
            .setValue(
                new Quantity()
                    .setValue(4.12)
                    .setUnit("10 trillion/L")
                    .setSystem("http://unitsofmeasure.org")
                    .setCode("10*12/L"));

    assertEquals("Patient/1", path.getString(obs, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(obs, "effective"));
    assertEquals(BigDecimal.valueOf(4.12), path.getNumber(obs, "value"));
    assertEquals("10 trillion/L", path.getString(obs, "value.unit"));
    assertEquals("http://unitsofmeasure.org", path.getString(obs, "value.system"));
    assertEquals("10*12/L", path.getString(obs, "value.code"));
  }

  @Test
  void testObservation2() {
    Observation obs = new Observation().setValue(new StringType("abc"));
    assertEquals("abc", path.getString(obs, "value"));
  }

  @Test
  void testObservation3() {
    Observation obs = new Observation().setValue(FHIRUtil.parse("2001-02-03T04:05"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(obs, "value"));
  }

  @Test
  void testObservation4() {
    Observation obs =
        new Observation()
            .setValue(
                new CodeableConcept(new Coding().setSystem("http://system.com").setCode("1234")));
    assertEquals(
        "http://system.com|1234",
        path.getString(obs, "value.coding.select(system.value + '|' + code)"));
  }

  @Test
  void testObservationComponent() {
    Observation obs =
        new Observation()
            .setValue(
                new CodeableConcept(new Coding().setSystem("http://system.com").setCode("1234")));

    assertEquals(
        "http://system.com|1234",
        path.getString(obs, "value.coding.select(system.value + '|' + code)"));

    obs.addComponent()
        .setCode(new CodeableConcept(new Coding().setSystem("http://system.com").setCode("xxx")))
        .setValue(new Quantity().setValue(111));

    obs.addComponent()
        .setCode(new CodeableConcept(new Coding().setSystem("http://system2.com").setCode("bbb")))
        .setValue(
            new CodeableConcept(new Coding().setSystem("http://value.system.com").setCode("ccc")));

    assertEquals(
        BigDecimal.valueOf(111),
        path.getNumber(
            obs,
            "component.where(code.coding.system.value + '|' + code.coding.code ="
                + " 'http://system.com|xxx').value.value"));

    assertEquals(
        "http://value.system.com|ccc",
        path.getString(
            obs,
            "component.where(code.coding.system.value + '|' + code.coding.code ="
                + " 'http://system2.com|bbb').value.coding.select(system.value + '|' + code)"));
  }

  @Test
  void testCondition() {
    Condition cond =
        new Condition()
            .setSubject(new Reference("Patient/1"))
            .setRecordedDate(DateUtil.parseToDate("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(cond, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(cond, "recordedDate"));
  }

  @Test
  void testMedicationAdministration() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setEffective(FHIRUtil.parse("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(med, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "effective"));
  }

  @Test
  void testProcedure() {
    Procedure proc =
        new Procedure()
            .setSubject(new Reference("Patient/1"))
            .setPerformed(FHIRUtil.parse("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(proc, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(proc, "performed"));
  }
}
