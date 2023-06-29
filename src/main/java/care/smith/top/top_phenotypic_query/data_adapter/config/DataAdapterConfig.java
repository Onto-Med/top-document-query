package care.smith.top.top_phenotypic_query.data_adapter.config;

import care.smith.top.model.Code;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAdapterConfig {

  private String id;
  private String adapter;
  private Map<String, String> connection = new HashMap<>();
  private CSVSettings csvSettings;
  private SubjectQuery subjectQuery;
  private Map<String, PhenotypeQuery> phenotypeQueries = new HashMap<>();
  private CodeMapping birthdateMapping;
  private CodeMapping ageMapping;
  private CodeMapping sexMapping;
  private Map<String, CodeMapping> codeMappings = new HashMap<>();

  public static DataAdapterConfig getInstance(String yamlFilePath) {
    try {
      return getInstanceFromStream(new FileInputStream(yamlFilePath));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static DataAdapterConfig getInstanceFromResource(String resourceName) {
    return getInstanceFromStream(
        DataAdapterConfig.class.getClassLoader().getResourceAsStream(resourceName));
  }

  public static DataAdapterConfig getInstanceFromStream(InputStream inputStream) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      return mapper.readValue(inputStream, DataAdapterConfig.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getAdapter() {
    return adapter;
  }

  public void setAdapter(String adapter) {
    this.adapter = adapter;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getConnection() {
    return connection;
  }

  public String getConnectionAttribute(String name) {
    return connection.get(name);
  }

  public void setConnection(Map<String, String> connection) {
    this.connection = connection;
  }

  public void setConnectionAttribute(String name, String value) {
    connection.put(name, value);
  }

  public CSVSettings getCsvSettings() {
    return csvSettings;
  }

  public void setCsvSettings(CSVSettings csvSettings) {
    this.csvSettings = csvSettings;
  }

  public SubjectQuery getSubjectQuery() {
    return subjectQuery;
  }

  public void setSubjectQuery(SubjectQuery subjectQuery) {
    this.subjectQuery = subjectQuery;
  }

  public Map<String, PhenotypeQuery> getPhenotypeQueries() {
    return phenotypeQueries;
  }

  public PhenotypeQuery getPhenotypeQuery(String type) {
    return phenotypeQueries.get(type);
  }

  public void setPhenotypeQueries(Map<String, PhenotypeQuery> phenotypeQueries) {
    this.phenotypeQueries = phenotypeQueries;
  }

  public CodeMapping getBirthdateMapping() {
    return birthdateMapping;
  }

  public void setBirthdateMapping(CodeMapping birthdateMapping) {
    this.birthdateMapping = birthdateMapping;
  }

  public boolean isBirthdate(Phenotype p) {
    if (Phenotypes.getItemType(p) == ItemType.SUBJECT_BIRTH_DATE) return true;
    if (Phenotypes.getUnrestrictedPhenotypeCodes(p) == null) return false;
    for (Code code : Phenotypes.getUnrestrictedPhenotypeCodes(p)) {
      if (birthdateMapping.getCode().equals(Phenotypes.getCodeUri(code))) return true;
    }
    return false;
  }

  public CodeMapping getAgeMapping() {
    return ageMapping;
  }

  public void setAgeMapping(CodeMapping ageMapping) {
    this.ageMapping = ageMapping;
  }

  public boolean isAge(Phenotype p) {
    if (Phenotypes.getItemType(p) == ItemType.SUBJECT_AGE) return true;
    if (Phenotypes.getUnrestrictedPhenotypeCodes(p) == null) return false;
    for (Code code : Phenotypes.getUnrestrictedPhenotypeCodes(p)) {
      if (ageMapping.getCode().equals(Phenotypes.getCodeUri(code))) return true;
    }
    return false;
  }

  public CodeMapping getSexMapping() {
    return sexMapping;
  }

  public void setSexMapping(CodeMapping sexMapping) {
    this.sexMapping = sexMapping;
  }

  public boolean isSex(Phenotype p) {
    if (Phenotypes.getItemType(p) == ItemType.SUBJECT_SEX) return true;
    if (Phenotypes.getUnrestrictedPhenotypeCodes(p) == null) return false;
    for (Code code : Phenotypes.getUnrestrictedPhenotypeCodes(p)) {
      if (sexMapping.getCode().equals(Phenotypes.getCodeUri(code))) return true;
    }
    return false;
  }

  public CodeMapping getCodeMapping(String code) {
    return codeMappings.get(code);
  }

  public CodeMapping getCodeMapping(Phenotype p) {
    if (Phenotypes.getUnrestrictedPhenotypeCodes(p) == null) return null;
    for (Code code : Phenotypes.getUnrestrictedPhenotypeCodes(p)) {
      CodeMapping map = getCodeMapping(Phenotypes.getCodeUri(code));
      if (map != null) return map;
    }
    return null;
  }

  public CodeMapping getCodeMappingIncludingSubjectParameters(Phenotype p) {
    if (isAge(p)) return getAgeMapping();
    if (isBirthdate(p)) return getBirthdateMapping();
    if (isSex(p)) return getSexMapping();
    return getCodeMapping(p);
  }

  public void setCodeMappings(List<CodeMapping> codeMappings) {
    if (codeMappings != null)
      for (CodeMapping cm : codeMappings) this.codeMappings.put(cm.getCode(), cm);
  }

  public List<CodeMapping> getCodeMappings() {
    return new ArrayList<>(codeMappings.values());
  }

  public DataAdapterConfig merge(DataAdapterConfig other) {
    setId(other.getId());
    setAdapter(other.getAdapter());
    setConnection(other.getConnection());
    if (other.getCsvSettings() != null) setCsvSettings(other.getCsvSettings());
    if (other.getSubjectQuery() != null) setSubjectQuery(other.getSubjectQuery());
    phenotypeQueries.putAll(other.getPhenotypeQueries());
    if (other.getBirthdateMapping() != null) setBirthdateMapping(other.getBirthdateMapping());
    if (other.getAgeMapping() != null) setAgeMapping(other.getAgeMapping());
    if (other.getSexMapping() != null) setSexMapping(other.getSexMapping());
    setCodeMappings(other.getCodeMappings());
    return this;
  }

  @Override
  public String toString() {
    return "DataAdapterConfig [id="
        + id
        + ", adapter="
        + adapter
        + ", connection="
        + connection
        + ", csvSettings="
        + csvSettings
        + ", subjectQuery="
        + subjectQuery
        + ", phenotypeQueries="
        + phenotypeQueries
        + ", birthdateMapping="
        + birthdateMapping
        + ", ageMapping="
        + ageMapping
        + ", sexMapping="
        + sexMapping
        + ", codeMappings="
        + codeMappings
        + "]";
  }
}
