package care.smith.top.top_phenotypic_query.data_adapter;

import care.smith.top.model.Expression;
import care.smith.top.model.Restriction;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.search.SingleSearch;
import care.smith.top.top_phenotypic_query.search.SubjectSearch;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataAdapter {

  protected DataAdapterConfig config;

  private Logger log = LoggerFactory.getLogger(DataAdapter.class);

  protected DataAdapter(DataAdapterConfig config) {
    this.config = config;
  }

  protected static DataAdapterConfig mergeDefault(DataAdapterConfig config, String defConfResName) {
    return DataAdapterConfig.getInstanceFromResource(
            "default_adapter_configuration/" + defConfResName + ".yml")
        .merge(config);
  }

  public static DataAdapter getInstance(DataAdapterConfig config) throws InstantiationException {
    DataAdapter dataAdapter;
    try {
      Class<?> adapterClass = Class.forName(config.getAdapter());
      dataAdapter =
          (DataAdapter) adapterClass.getConstructor(DataAdapterConfig.class).newInstance(config);
    } catch (ClassNotFoundException
        | InvocationTargetException
        | IllegalAccessException
        | NoSuchMethodException
        | ClassCastException e) {
      e.printStackTrace();
      throw new InstantiationException("Could not instantiate adapter for provided configuration.");
    }
    return dataAdapter;
  }

  public static DataAdapter getInstance(String configFile) throws InstantiationException {
    return getInstance(DataAdapterConfig.getInstance(configFile));
  }

  public static DataAdapter getInstanceFromResource(String configFile)
      throws InstantiationException {
    return getInstance(DataAdapterConfig.getInstanceFromResource(configFile));
  }

  public DataAdapterConfig getConfig() {
    return config;
  }

  // call terminology server
  // fetch id attributes, column/table names
  // build atomic queries using DataAdapterConfig
  // execute queries and normalize/return ResultSet
  public abstract ResultSet execute(SubjectSearch search) throws SQLException;

  public abstract ResultSet execute(SingleSearch search) throws SQLException;

  public abstract DataAdapterSettings getSettings();

  public abstract void close();

  public void checkQuantifier(SingleSearch search, ResultSet rs) {
    if (!search.hasRestriction()) return;
    Restriction r = search.getSourceRestriction();
    if (Restrictions.hasExistentialQuantifier(r)) {
      replacePhenotypes(search, rs);
      return;
    }

    Entities phes = Entities.of(search.getPhenotype(), search.getSuperPhenotype());

    for (String sbjId : new HashSet<>(rs.getSubjectIds())) {
      SubjectPhenotypes sbjPhes = rs.getPhenotypes(sbjId);
      Expression resExp =
          new C2R()
              .phenotypes(phes)
              .values(sbjPhes)
              .dateTimeRestriction(search.getDateTimeRestriction())
              .calculate(search.getPhenotype().getExpression());

      boolean isOK = Expressions.getBooleanValue(resExp);

      log.debug(
          "check quantifier: {}::{}::{}::{}",
          sbjId,
          search.getPhenotype().getId(),
          Restrictions.toString(r),
          isOK);

      if (isOK) replacePhenotype(sbjId, search, rs);
      else rs.removeSubject(sbjId);
    }
  }

  private void replacePhenotype(String sbjId, SingleSearch search, ResultSet rs) {
    rs.replacePhenotype(
        sbjId,
        search.getSuperPhenotype().getId(),
        Phenotypes.getRestrictedValuesKey(search.getPhenotype()));
  }

  private void replacePhenotypes(SingleSearch search, ResultSet rs) {
    for (String sbjId : new HashSet<>(rs.getSubjectIds())) replacePhenotype(sbjId, search, rs);
  }
}
