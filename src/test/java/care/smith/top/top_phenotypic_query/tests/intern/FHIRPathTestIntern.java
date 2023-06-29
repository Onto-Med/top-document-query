package care.smith.top.top_phenotypic_query.tests.intern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.uhn.fhir.context.FhirContext;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRPath;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRUtil;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationDosageComponent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.Timing.TimingRepeatComponent;
import org.junit.jupiter.api.Test;

public class FHIRPathTestIntern {

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
  void testObservation5() {
    Observation obs =
        new Observation()
            .setSubject(new Reference("Patient/1"))
            .setEffective(
                new Period()
                    .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                    .setEndElement(FHIRUtil.parse("2005-06-07T08:09")));

    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(obs, "effective.start"));
    assertEquals(LocalDateTime.of(2005, 6, 7, 8, 9), path.getDateTime(obs, "effective.end"));
    assertNull(path.getDateTime(obs, "effective"));
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
  void testCondition1() {
    Condition cond =
        new Condition()
            .setSubject(new Reference("Patient/1"))
            .setOnset(new DateTimeType("2001-02-03T04:05:00"));

    assertEquals("Patient/1", path.getString(cond, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(cond, "onset"));
    assertNull(path.getDateTime(cond, "onset.start"));
    assertNull(path.getDateTime(cond, "onset.end"));
  }

  @Test
  void testCondition2() {
    Condition cond =
        new Condition()
            .setSubject(new Reference("Patient/1"))
            .setOnset(
                new Period()
                    .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                    .setEndElement(FHIRUtil.parse("2005-06-07T08:09")));

    assertEquals("Patient/1", path.getString(cond, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(cond, "onset.start"));
    assertEquals(LocalDateTime.of(2005, 6, 7, 8, 9), path.getDateTime(cond, "onset.end"));
    assertNull(path.getDateTime(cond, "onset"));
  }

  @Test
  void testMedicationAdministration1() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setEffective(FHIRUtil.parse("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(med, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "effective"));
    assertNull(path.getDateTime(med, "effective.start"));
    assertNull(path.getDateTime(med, "effective.end"));
  }

  @Test
  void testMedicationAdministration2() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setEffective(
                new Period()
                    .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                    .setEndElement(FHIRUtil.parse("2005-06-07T08:09")));

    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "effective.start"));
    assertEquals(LocalDateTime.of(2005, 6, 7, 8, 9), path.getDateTime(med, "effective.end"));
    assertNull(path.getDateTime(med, "effective"));
  }

  @Test
  void testMedicationAdministration3() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setDosage(
                new MedicationAdministrationDosageComponent()
                    .setDose(
                        new Quantity()
                            .setValue(4.5)
                            .setUnit("g")
                            .setSystem("http://unitsofmeasure.org")
                            .setCode("g"))
                    .setRate(
                        new Ratio()
                            .setNumerator(
                                new Quantity()
                                    .setValue(8)
                                    .setUnit("ml")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("ml"))
                            .setDenominator(
                                new Quantity()
                                    .setValue(1)
                                    .setUnit("min")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("min"))));

    assertEquals(BigDecimal.valueOf(4.5), path.getNumber(med, "dosage.dose.value"));
    assertEquals(BigDecimal.valueOf(8), path.getNumber(med, "dosage.rate.numerator"));
    assertEquals(BigDecimal.valueOf(1), path.getNumber(med, "dosage.rate.denominator"));
  }

  @Test
  void testMedicationAdministration4() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setDosage(
                new MedicationAdministrationDosageComponent()
                    .setDose(
                        new Quantity()
                            .setValue(4.5)
                            .setUnit("g")
                            .setSystem("http://unitsofmeasure.org")
                            .setCode("g"))
                    .setRate(
                        new Quantity()
                            .setValue(8)
                            .setUnit("ml")
                            .setSystem("http://unitsofmeasure.org")
                            .setCode("ml")));

    assertEquals(BigDecimal.valueOf(4.5), path.getNumber(med, "dosage.dose.value"));
    assertEquals(BigDecimal.valueOf(8), path.getNumber(med, "dosage.rate.value"));
  }

  @Test
  void testMedicationAdministration5() {
    MedicationAdministration med =
        new MedicationAdministration()
            .setSubject(new Reference("Patient/1"))
            .setDosage(
                new MedicationAdministrationDosageComponent()
                    .setDose(
                        new Quantity()
                            .setValue(2)
                            .setUnit("TAB")
                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm")
                            .setCode("TAB"))
                    .setRoute(
                        new CodeableConcept(
                            new Coding()
                                .setSystem("http://snomed.info/sct")
                                .setCode("26643006")
                                .setDisplay("Oral Route")))
                    .setMethod(
                        new CodeableConcept(
                            new Coding()
                                .setSystem("http://snomed.info/sct")
                                .setCode("421521009")
                                .setDisplay(
                                    "Swallow - dosing instruction imperative (qualifier value)"))));

    assertEquals(BigDecimal.valueOf(2), path.getNumber(med, "dosage.dose.value"));
    assertEquals(
        "http://snomed.info/sct|26643006",
        path.getString(med, "dosage.route.coding.select(system.value + '|' + code)"));
    assertEquals("Oral Route", path.getString(med, "dosage.route.coding.display"));
    assertEquals(
        "http://snomed.info/sct|421521009",
        path.getString(med, "dosage.method.coding.select(system.value + '|' + code)"));
    assertEquals(
        "Swallow - dosing instruction imperative (qualifier value)",
        path.getString(med, "dosage.method.coding.display"));
  }

  @Test
  void testMedicationRequest() {
    MedicationRequest med =
        new MedicationRequest()
            .setSubject(new Reference("Patient/1"))
            .setAuthoredOnElement(FHIRUtil.parse("2001-02-03T04:05"))
            .addDosageInstruction(
                new Dosage()
                    .setTiming(
                        new Timing()
                            .setRepeat(
                                new TimingRepeatComponent()
                                    .setBounds(
                                        new Period()
                                            .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                                            .setEndElement(FHIRUtil.parse("2005-06-07T08:09"))))));

    assertEquals("Patient/1", path.getString(med, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "authoredOn"));
    assertEquals(
        LocalDateTime.of(2001, 2, 3, 4, 5),
        path.getDateTime(med, "dosageInstruction.timing.repeat.bounds.start"));
    assertEquals(
        LocalDateTime.of(2005, 6, 7, 8, 9),
        path.getDateTime(med, "dosageInstruction.timing.repeat.bounds.end"));
  }

  @Test
  void testMedicationStatement1() {
    MedicationStatement med =
        new MedicationStatement()
            .setSubject(new Reference("Patient/1"))
            .setEffective(FHIRUtil.parse("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(med, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "effective"));
    assertNull(path.getDateTime(med, "effective.start"));
    assertNull(path.getDateTime(med, "effective.end"));
  }

  @Test
  void testMedicationStatement2() {
    MedicationStatement med =
        new MedicationStatement()
            .setSubject(new Reference("Patient/1"))
            .setEffective(
                new Period()
                    .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                    .setEndElement(FHIRUtil.parse("2005-06-07T08:09")));

    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(med, "effective.start"));
    assertEquals(LocalDateTime.of(2005, 6, 7, 8, 9), path.getDateTime(med, "effective.end"));
    assertNull(path.getDateTime(med, "effective"));
  }

  @Test
  void testProcedure1() {
    Procedure proc =
        new Procedure()
            .setSubject(new Reference("Patient/1"))
            .setPerformed(FHIRUtil.parse("2001-02-03T04:05"));

    assertEquals("Patient/1", path.getString(proc, "subject.reference.value"));
    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(proc, "performed"));
  }

  @Test
  void testProcedure2() {
    Procedure proc =
        new Procedure()
            .setSubject(new Reference("Patient/1"))
            .setPerformed(
                new Period()
                    .setStartElement(FHIRUtil.parse("2001-02-03T04:05"))
                    .setEndElement(FHIRUtil.parse("2005-06-07T08:09")));

    assertEquals(LocalDateTime.of(2001, 2, 3, 4, 5), path.getDateTime(proc, "performed.start"));
    assertEquals(LocalDateTime.of(2005, 6, 7, 8, 9), path.getDateTime(proc, "performed.end"));
    assertNull(path.getDateTime(proc, "performed"));
  }
}
