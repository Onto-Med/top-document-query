package care.smith.top.top_phenotypic_query.search;

import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.model.ProjectionEntry;
import care.smith.top.model.Query;
import care.smith.top.model.Restriction;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapter;
import care.smith.top.top_phenotypic_query.data_adapter.config.CodeMapping;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeOutput;
import care.smith.top.top_phenotypic_query.data_adapter.config.PhenotypeQuery;
import care.smith.top.top_phenotypic_query.data_adapter.config.Props;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class SingleSearch extends PhenotypeSearch {

  private ProjectionEntry entry;
  private Phenotype phenotype;
  private DataAdapter adapter;
  private DataAdapterConfig config;

  public SingleSearch(
      Query query, ProjectionEntry entry, Phenotype phenotype, DataAdapter adapter) {
    super(query);
    this.entry = entry;
    this.phenotype = phenotype;
    this.adapter = adapter;
    this.config = adapter.getConfig();
  }

  public ProjectionEntry getEntry() {
    return entry;
  }

  public Phenotype getPhenotype() {
    return phenotype;
  }

  public Phenotype getSuperPhenotype() {
    return phenotype.getSuperPhenotype();
  }

  public boolean hasDateTimeRestriction() {
    return entry.getDateTimeRestriction() != null;
  }

  public DateTimeRestriction getDateTimeRestriction() {
    return entry.getDateTimeRestriction();
  }

  public CodeMapping getCodeMapping() {
    return config.getCodeMapping(phenotype);
  }

  public Restriction getSourceRestriction() {
    return (getCodeMapping() == null)
        ? getRestriction()
        : getCodeMapping().getSourceRestriction(getRestriction(), getSuperPhenotype());
  }

  public String getModelUnit() {
    return phenotype.getUnit();
  }

  public String getSourceUnit() {
    return (getCodeMapping() == null) ? null : getCodeMapping().getUnit();
  }

  public String getType() {
    if (getCodeMapping() != null && getCodeMapping().getType() != null)
      return getCodeMapping().getType();
    if (config.getPhenotypeQuery(Phenotypes.getItemType(phenotype).getValue()) != null)
      return phenotype.getItemType().getValue();
    return Props.DEFAULT_ITEM_TYPE;
  }

  public Map<String, String> getPhenotypeMappings() {
    if (getCodeMapping() == null) return null;
    return getCodeMapping().getPhenotypeMappings();
  }

  public PhenotypeQuery getPhenotypeQuery() {
    return config.getPhenotypeQuery(getType());
  }

  public PhenotypeOutput getOutput() {
    return getPhenotypeQuery().getOutput().mapping(getPhenotypeMappings());
  }

  public boolean hasRestriction() {
    return Phenotypes.isSingleRestriction(phenotype);
  }

  public Restriction getRestriction() {
    return phenotype.getRestriction();
  }

  public DataAdapter getAdapter() {
    return adapter;
  }

  public DataAdapterConfig getAdapterConfig() {
    return config;
  }

  @Override
  public ResultSet execute() throws SQLException {
    String itemType = Phenotypes.getItemType(phenotype).getValue();
    PhenotypeQuery query = config.getPhenotypeQuery(itemType);
    if (query != null && query.hasUnion()) {
      ResultSet union = new ResultSet();
      for (String partItemType : query.getUnion())
        union = union.unite(getUnionPart(partItemType).execute());
      return union;
    }
    return adapter.execute(this);
  }

  private SingleSearch getUnionPart(String itemType) {
    Phenotype unionPartPhe = null;
    if (hasRestriction()) {
      Phenotype supP = clone(phenotype.getSuperPhenotype(), itemType);
      unionPartPhe = Phenotypes.clone(phenotype).superPhenotype(supP);
    } else unionPartPhe = clone(phenotype, itemType);
    return new SingleSearch(getQuery(), entry, unionPartPhe, adapter);
  }

  private Phenotype clone(Phenotype p, String itemType) {
    return Phenotypes.clone(p).itemType(ItemType.fromValue(itemType));
  }

  @Override
  public int hashCode() {
    return Objects.hash(entry.getDateTimeRestriction(), phenotype.getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SingleSearch other = (SingleSearch) obj;
    return Objects.equals(entry.getDateTimeRestriction(), other.entry.getDateTimeRestriction())
        && Objects.equals(phenotype.getId(), other.phenotype.getId());
  }

  @Override
  public String toString() {
    return "SingleSearch: "
        + phenotype.getId()
        + "::"
        + Restrictions.toString(getDateTimeRestriction());
  }
}
