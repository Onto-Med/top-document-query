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
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

public class FHIRClientTestIntern {

  private static final String SYSTEM = "https://www.top-test.de/system";
  private static FHIRClient client;

  public static void main(String[] args) throws SQLException {
    URL configFile =
        Thread.currentThread().getContextClassLoader().getResource("config/FHIR_Adapter_Test.yml");
    DataAdapterConfig conf = DataAdapterConfig.getInstance(configFile.getPath());
    client = new FHIRClient(conf);

    client.deleteAllResources(SYSTEM);

    convert();
    System.exit(0);

    Patient patient = new Patient();
    patient.addIdentifier().setSystem(SYSTEM).setValue("1");
    patient.addName().setFamily("Jameson").addGiven("J").addGiven("Jonah");
    patient.setGender(Enumerations.AdministrativeGender.MALE);
    patient.setBirthDate(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));

    String patId = client.createResource(patient).get();

    System.out.println(patId);

    Observation observation =
        new Observation()
            .setValue(
                new Quantity()
                    .setValue(4.12)
                    .setUnit("10 trillion/L")
                    .setSystem("http://unitsofmeasure.org")
                    .setCode("10*12/L"))
            .setSubject(new Reference("Patient/" + patId));
    observation.addIdentifier().setSystem(SYSTEM).setValue("2");
    observation.setEffective(
        new DateTimeType(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0))));

    String obsId = client.createResource(observation).get();

    System.out.println(obsId);
  }

  private static void convert() throws SQLException {
    DataAdapterConfig sqlConfig =
        DataAdapterConfig.getInstance("test_files/SQL_Adapter_Test_intern.yml");
    SQLAdapter sqlAdapter = new SQLAdapter(sqlConfig);
    ResultSet rs =
        sqlAdapter.executeQuery(
            "SELECT s.subject_id, birth_date, sex, assessment_id, created_at, height, weight FROM"
                + " subject s, assessment1 a WHERE s.subject_id = a.subject_id");

    Map<String, String> patIds = new HashMap<>();

    while (rs.next()) {
      String patId = rs.getString("subject_id");
      LocalDateTime bd = rs.getTimestamp("birth_date").toLocalDateTime();
      String sex = rs.getString("sex");
      String obsId = rs.getString("assessment_id");
      LocalDateTime date = rs.getTimestamp("created_at").toLocalDateTime();
      BigDecimal height = rs.getBigDecimal("height");
      BigDecimal weight = rs.getBigDecimal("weight");

      String fhirPatId = patIds.get(patId);
      if (fhirPatId == null) {
        fhirPatId = createPatient(patId, bd, sex);
        patIds.put(patId, fhirPatId);
      }

      if (height != null) createHeight("h" + obsId, fhirPatId, height, date);

      if (weight != null) createWeight("w" + obsId, fhirPatId, weight, date);
    }
  }

  private static String createPatient(String id, LocalDateTime bd, String sex) {
    Patient patient = new Patient();
    patient.addIdentifier().setSystem(SYSTEM).setValue(id);
    patient.setGender(Enumerations.AdministrativeGender.valueOf(sex.toUpperCase()));
    patient.setBirthDate(Timestamp.valueOf(bd));
    return client.createResource(patient).get();
  }

  private static void createHeight(
      String id, String fhirPatId, BigDecimal val, LocalDateTime date) {
    createObservation(id, fhirPatId, val, date, "cm", "http://loinc.org", "3137-7");
  }

  private static void createWeight(
      String id, String fhirPatId, BigDecimal val, LocalDateTime date) {
    createObservation(id, fhirPatId, val, date, "kg", "http://loinc.org", "3141-9");
  }

  private static void createObservation(
      String id,
      String fhirPatId,
      BigDecimal val,
      LocalDateTime date,
      String unit,
      String system,
      String code) {
    Quantity v =
        new Quantity()
            .setValue(val)
            .setUnit(unit)
            .setSystem("http://unitsofmeasure.org")
            .setCode(unit);
    Observation observation =
        new Observation().setValue(v).setSubject(new Reference("Patient/" + fhirPatId));
    observation.addIdentifier().setSystem(SYSTEM).setValue(id);
    observation.setEffective(new DateTimeType(Timestamp.valueOf(date)));
    observation.setCode(new CodeableConcept(new Coding().setSystem(system).setCode(code)));
    client.createResource(observation).get();
  }
}
