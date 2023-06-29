package care.smith.top.top_phenotypic_query.converter.csv;

import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import java.util.List;

public class CSVMetadataRecord extends CSVRecordCompositeEntries {

  private static final long serialVersionUID = 1L;

  public static List<String> FIELDS =
      List.of(
          "phenotype",
          "parent",
          "type",
          "itemtype",
          "datatype",
          "unit",
          "titles",
          "synonyms",
          "descriptions",
          "codes",
          "restriction",
          "expression");

  public CSVMetadataRecord(Phenotype phe, String entryPartsDelimiter) {
    super(entryPartsDelimiter);
    add(phe.getId());
    addParent(phe);
    addType(phe);
    addItemType(phe);
    add(phe.getDataType().getValue());
    addEntry(phe.getUnit());
    addEntry(Entities.getTitles(phe));
    addEntry(Entities.getSynonyms(phe));
    addEntry(Entities.getDescriptions(phe));
    addEntry(Phenotypes.getCodeUris(phe));
    addRestriction(phe);
    addExpression(phe);
  }

  private void addParent(Phenotype phe) {
    if (phe.getSuperPhenotype() == null) add("");
    else add(phe.getSuperPhenotype().getId());
  }

  private void addType(Phenotype phe) {
    if (phe.getEntityType() == null) add("");
    else add(phe.getEntityType().getValue());
  }

  private void addItemType(Phenotype phe) {
    if (phe.getItemType() == null) add("");
    else add(phe.getItemType().getValue());
  }

  private void addRestriction(Phenotype phe) {
    if (phe.getRestriction() == null) add("");
    else add(Restrictions.toString(phe.getRestriction()));
  }

  private void addExpression(Phenotype phe) {
    if (phe.getRestriction() != null || phe.getExpression() == null) add("");
    else add(new C2R().toString(phe.getExpression()));
  }
}
