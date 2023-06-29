package care.smith.top.top_phenotypic_query.data_adapter.sql;

import care.smith.top.model.Code;
import care.smith.top.model.CodeSystem;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.EntityType;
import care.smith.top.model.NumberRestriction;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringRestriction;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapterSettings;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeOutput;
import care.smith.top.top_phenotypic_query.data_adapter.config.SubjectOutput;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLAdapter extends DataAdapter {

  private Connection con;
  private static final Logger log = LoggerFactory.getLogger(SQLAdapter.class);

  public SQLAdapter(DataAdapterConfig config) throws SQLException {
    super(mergeDefault(config, "Default_SQL_Adapter"));
    initConnection();
  }

  private void initConnection() throws SQLException {
    this.con =
        DriverManager.getConnection(
            config.getConnectionAttribute("url"),
            config.getConnectionAttribute("user"),
            config.getConnectionAttribute("password"));
  }

  public java.sql.ResultSet executeQuery(String query) throws SQLException {
    return con.createStatement().executeQuery(query);
  }

  public Connection getConnection() {
    return con;
  }

  private LocalDateTime getDate(String dateCol, java.sql.ResultSet sqlRS) throws SQLException {
    if (dateCol == null) return null;
    return sqlRS.getTimestamp(dateCol).toLocalDateTime();
  }

  @Override
  public ResultSet execute(SingleSearch search) throws SQLException {
    ResultSet rs = new ResultSet();

    String preparedQuery = SQLAdapterSettings.get().createSinglePreparedQuery(search);
    PreparedStatement ps =
        SQLAdapterSettings.get().getSinglePreparedStatement(preparedQuery, con, search);
    log.debug("Execute SQL query: {}", ps);
    java.sql.ResultSet sqlRS = ps.executeQuery();
    Phenotype phe = search.getPhenotype();
    PhenotypeOutput out = search.getOutput();
    String sbjCol = out.getSubject();
    String pheCol = out.getValue(Phenotypes.getDataType(phe));
    String dateCol = out.getDateTime();
    String startDateCol = out.getStartDateTime();
    String endDateCol = out.getEndDateTime();

    while (sqlRS.next()) {
      String sbj = sqlRS.getString(sbjCol);
      LocalDateTime date = getDate(dateCol, sqlRS);
      LocalDateTime startDate = getDate(startDateCol, sqlRS);
      LocalDateTime endDate = getDate(endDateCol, sqlRS);
      Value val = null;
      if (Phenotypes.hasBooleanType(phe)) {
        if (pheCol == null) {
          rs.addValue(
              sbj, phe, search.getDateTimeRestriction(), Val.ofTrue(date, startDate, endDate));
          continue;
        } else val = Val.of(sqlRS.getBoolean(pheCol), date, startDate, endDate);
      } else if (Phenotypes.hasDateTimeType(phe))
        val = Val.of(sqlRS.getTimestamp(pheCol).toLocalDateTime(), date, startDate, endDate);
      else if (Phenotypes.hasNumberType(phe))
        val = Val.of(sqlRS.getBigDecimal(pheCol), date, startDate, endDate);
      else val = Val.of(sqlRS.getString(pheCol), date, startDate, endDate);
      if (val != null)
        rs.addValueWithRestriction(
            sbj,
            phe,
            search.getDateTimeRestriction(),
            val,
            search.getSourceUnit(),
            search.getModelUnit());
    }
    checkQuantifier(search, rs);

    return rs;
  }

  @Override
  public ResultSet execute(SubjectSearch search) throws SQLException {
    ResultSet rs = new ResultSet();

    String preparedQuery = SQLAdapterSettings.get().createSubjectPreparedQuery(search);
    PreparedStatement ps =
        SQLAdapterSettings.get().getSubjectPreparedStatement(preparedQuery, con, search);
    log.debug("Execute SQL query: {}", ps);
    java.sql.ResultSet sqlRS = ps.executeQuery();
    SubjectOutput out = search.getOutput();
    String sbjCol = out.getId();
    String bdCol = out.getBirthdate();
    String sexCol = out.getSex();
    Phenotype sex = search.getSex();
    Phenotype bd = search.getBirthdateDerived();
    Phenotype age = search.getAge();

    while (sqlRS.next()) {
      String sbj = sqlRS.getString(sbjCol);
      if (bd == null && sex == null) {
        rs.addSubject(sqlRS.getString(sbjCol));
        continue;
      }
      if (bd != null) {
        Timestamp bdSqlVal = sqlRS.getTimestamp(bdCol);
        if (bdSqlVal != null) {
          Value val = Val.of(bdSqlVal.toLocalDateTime());
          if (search.getBirthdate() != null) rs.addValueWithRestriction(sbj, bd, val);
          else rs.addValue(sbj, bd, null, val);
          if (age != null) {
            Value ageVal = Val.of(SubjectSearch.birthdateToAge(bdSqlVal.toLocalDateTime()));
            rs.addValueWithRestriction(sbj, age, ageVal);
          }
        }
      }
      if (sex != null) {
        if (Phenotypes.hasBooleanType(sex)) {
          Boolean sexSqlVal = sqlRS.getBoolean(sexCol);
          if (sexSqlVal != null) rs.addValueWithRestriction(sbj, sex, Val.of(sexSqlVal));
        } else if (Phenotypes.hasNumberType(sex)) {
          BigDecimal sexSqlVal = sqlRS.getBigDecimal(sexCol);
          if (sexSqlVal != null) rs.addValueWithRestriction(sbj, sex, Val.of(sexSqlVal));
        } else {
          String sexSqlVal = sqlRS.getString(sexCol);
          if (sexSqlVal != null) rs.addValueWithRestriction(sbj, sex, Val.of(sexSqlVal));
        }
      }
    }

    return rs;
  }

  @Override
  public DataAdapterSettings getSettings() {
    return SQLAdapterSettings.get();
  }

  @Override
  public void close() {
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void printSize(java.sql.ResultSet rs) {
    if (rs == null) System.out.println("SIZE: NULL");
    else {
      try {
        rs.last();
        System.out.println("SIZE: " + rs.getRow());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void print(java.sql.ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnsNumber = rsmd.getColumnCount();
      while (rs.next()) {
        for (int i = 1; i <= columnsNumber; i++) {
          if (i > 1) System.out.print(" | ");
          System.out.print(rs.getString(i));
        }
        System.out.println("");
      }
      for (int i = 1; i <= columnsNumber; i++) {
        if (i > 1) System.out.print(" | ");
        System.out.print(rsmd.getColumnName(i));
      }
      System.out.println("");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws URISyntaxException, SQLException {
    DataAdapterConfig conf = DataAdapterConfig.getInstance("test_files/Simple_SQL_Config.yaml");
    SQLAdapter sql = new SQLAdapter(conf);

    //    Phenotype phe = new Phenotype().dataType(DataType.NUMBER).itemType(ItemType.OBSERVATION);
    //    phe.setId("weight");
    //    phe.setEntityType(EntityType.SINGLE_PHENOTYPE);
    //    phe.addCodesItem(
    //        new Code().code("3141-9").codeSystem(new CodeSystem().uri(new
    // URI("http://loinc.org"))));
    //    SingleSearch search = new SingleSearch(null, new QueryCriterion().subject(phe), sql);
    //    System.out.println(sql.execute(search));

    Phenotype bd = new Phenotype().dataType(DataType.DATE_TIME);
    bd.setId("birthdate");
    bd.setEntityType(EntityType.SINGLE_PHENOTYPE);
    bd.addCodesItem(
        new Code().code("21112-8").codeSystem(new CodeSystem().uri(new URI("http://loinc.org"))));

    DateTimeRestriction bdR =
        new DateTimeRestriction().addValuesItem(LocalDateTime.of(2000, 1, 1, 0, 0));
    bdR.setType(DataType.DATE_TIME);
    bdR.setQuantifier(Quantifier.MIN);
    bdR.setCardinality(1);
    bdR.setMinOperator(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO);

    Phenotype young = new Phenotype().dataType(DataType.DATE_TIME).restriction(bdR);
    young.setId("young");
    young.setEntityType(EntityType.SINGLE_RESTRICTION);
    young.setSuperPhenotype(bd);

    Phenotype age = new Phenotype().dataType(DataType.NUMBER);
    age.setId("age");
    age.setEntityType(EntityType.SINGLE_PHENOTYPE);
    age.addCodesItem(
        new Code().code("30525-0").codeSystem(new CodeSystem().uri(new URI("http://loinc.org"))));

    NumberRestriction ageR = new NumberRestriction().addValuesItem(BigDecimal.valueOf(20));
    ageR.setType(DataType.NUMBER);
    ageR.setQuantifier(Quantifier.MIN);
    ageR.setCardinality(1);
    ageR.setMaxOperator(RestrictionOperator.LESS_THAN_OR_EQUAL_TO);

    Phenotype youngAge = new Phenotype().dataType(DataType.NUMBER).restriction(ageR);
    youngAge.setId("youngAge");
    youngAge.setEntityType(EntityType.SINGLE_RESTRICTION);
    youngAge.setSuperPhenotype(age);

    Phenotype sex = new Phenotype().dataType(DataType.STRING);
    sex.setId("sex");
    sex.setEntityType(EntityType.SINGLE_PHENOTYPE);
    sex.addCodesItem(
        new Code().code("46098-0").codeSystem(new CodeSystem().uri(new URI("http://loinc.org"))));

    StringRestriction femaleR =
        new StringRestriction().addValuesItem("http://hl7.org/fhir/administrative-gender|female");
    femaleR.setType(DataType.STRING);
    femaleR.setQuantifier(Quantifier.MIN);
    femaleR.setCardinality(1);

    Phenotype female = new Phenotype().dataType(DataType.STRING).restriction(femaleR);
    female.setId("female");
    female.setEntityType(EntityType.SINGLE_RESTRICTION);
    female.setSuperPhenotype(sex);

    SubjectSearch sbjSearch = new SubjectSearch(null, female, null, youngAge, sql);
    ResultSet rs = sql.execute(sbjSearch);
    System.out.println(rs);
    System.out.println("SIZE: " + rs.size());
  }
}
