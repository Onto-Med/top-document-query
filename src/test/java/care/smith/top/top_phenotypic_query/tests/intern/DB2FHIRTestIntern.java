package care.smith.top.top_phenotypic_query.tests.intern;

import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.fhir.FHIRClient;
import care.smith.top.top_phenotypic_query.data_adapter.sql.SQLAdapter;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

public class DB2FHIRTestIntern {

  private static final String SYSTEM = "https://www.top-test.de/system";
  private static FHIRClient client;

  public static void main(String[] args) throws SQLException {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/FHIR_Adapter_Test.yml");
    DataAdapterConfig conf = DataAdapterConfig.getInstance(configFile.getPath());
    client = new FHIRClient(conf);
    client.deleteAllResources(SYSTEM);

    convert();
  }

  private static void convert() throws SQLException {
    URL configFile =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource("config/Delir_SQL_Adapter_Test.yml");
    DataAdapterConfig sqlConfig = DataAdapterConfig.getInstance(configFile.getPath());
    SQLAdapter sqlAdapter = new SQLAdapter(sqlConfig);

    ResultSet subjects = sqlAdapter.executeQuery("SELECT subject_id, birth_date, sex FROM subject");

    Map<String, String> patIds = new HashMap<>();

    while (subjects.next()) {
      String patId = subjects.getString("subject_id");
      LocalDateTime bd = subjects.getTimestamp("birth_date").toLocalDateTime();
      String sex = subjects.getString("sex");

      String fhirPatId = patIds.get(patId);
      if (fhirPatId == null) {
        fhirPatId = createPatient(patId, bd, sex);
        patIds.put(patId, fhirPatId);
      }
    }

    ResultSet observations =
        sqlAdapter.executeQuery(
            "SELECT phenotype_id, subject_id, created_at, code_system, code, unit, number_value"
                + " FROM observation");
    while (observations.next()) {
      Phenotype p = new Phenotype(observations, patIds, true);
      if (p.getValue() != null) createObservation(p);
    }

    ResultSet conditions =
        sqlAdapter.executeQuery(
            "SELECT phenotype_id, subject_id, created_at, code_system, code FROM condition");
    while (conditions.next()) createCondition(new Phenotype(conditions, patIds, false));

    ResultSet medications =
        sqlAdapter.executeQuery(
            "SELECT phenotype_id, subject_id, created_at, code_system, code FROM medication");
    while (medications.next()) createMedication(new Phenotype(medications, patIds, false));

    ResultSet procedures =
        sqlAdapter.executeQuery(
            "SELECT phenotype_id, subject_id, created_at, code_system, code FROM procedure");
    while (procedures.next()) createProcedure(new Phenotype(procedures, patIds, false));
  }

  private static String createPatient(String id, LocalDateTime bd, String sex) {
    Patient patient = new Patient();
    patient.addIdentifier().setSystem(SYSTEM).setValue(id);
    patient.setGender(Enumerations.AdministrativeGender.valueOf(sex.toUpperCase()));
    patient.setBirthDate(Timestamp.valueOf(bd));
    return client.createResource(patient).get();
  }

  private static void createObservation(Phenotype p) {
    Quantity v =
        new Quantity()
            .setValue(p.getValue())
            .setUnit(p.getUnit())
            .setSystem("http://unitsofmeasure.org")
            .setCode(p.getUnit());
    Observation observation =
        new Observation().setValue(v).setSubject(new Reference("Patient/" + p.getPatientId()));
    observation.addIdentifier().setSystem(SYSTEM).setValue(p.getId());
    observation.setEffective(new DateTimeType(p.getDate()));
    observation.setCode(
        new CodeableConcept(new Coding().setSystem(p.getSystem()).setCode(p.getCode())));
    client.createResource(observation).get();
  }

  private static void createCondition(Phenotype p) {
    Condition condition = new Condition().setSubject(new Reference("Patient/" + p.getPatientId()));
    condition.addIdentifier().setSystem(SYSTEM).setValue(p.getId());
    condition.setRecordedDate(p.getDate());
    condition.setCode(
        new CodeableConcept(new Coding().setSystem(p.getSystem()).setCode(p.getCode())));
    client.createResource(condition).get();
  }

  private static void createProcedure(Phenotype p) {
    Procedure procedure = new Procedure().setSubject(new Reference("Patient/" + p.getPatientId()));
    procedure.addIdentifier().setSystem(SYSTEM).setValue(p.getId());
    procedure.setPerformed(new DateTimeType(p.getDate()));
    procedure.setCode(
        new CodeableConcept(new Coding().setSystem(p.getSystem()).setCode(p.getCode())));
    client.createResource(procedure).get();
  }

  private static void createMedication(Phenotype p) {
    MedicationAdministration med =
        new MedicationAdministration().setSubject(new Reference("Patient/" + p.getPatientId()));
    med.addIdentifier().setSystem(SYSTEM).setValue(p.getId());
    med.setEffective(new DateTimeType(p.getDate()));
    med.setMedication(
        new CodeableConcept(new Coding().setSystem(p.getSystem()).setCode(p.getCode())));
    client.createResource(med).get();
  }

  static class Phenotype {
    String id;
    String patientId;
    Date date;
    String system;
    String code;
    String unit;
    BigDecimal value;

    Phenotype(ResultSet rs, Map<String, String> patIds, boolean withValue) throws SQLException {
      id = rs.getString("phenotype_id");
      patientId = patIds.get(rs.getString("subject_id"));
      date = rs.getTimestamp("created_at");
      system = rs.getString("code_system");
      code = rs.getString("code");
      if (withValue) {
        unit = rs.getString("unit");
        value = rs.getBigDecimal("number_value");
      }
    }

    String getId() {
      return id;
    }

    String getPatientId() {
      return patientId;
    }

    Date getDate() {
      return date;
    }

    String getSystem() {
      return system;
    }

    String getCode() {
      return code;
    }

    String getUnit() {
      return unit;
    }

    BigDecimal getValue() {
      return value;
    }
  }
}
