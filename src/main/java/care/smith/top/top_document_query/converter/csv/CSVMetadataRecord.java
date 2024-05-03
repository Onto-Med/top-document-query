package care.smith.top.top_document_query.converter.csv;

import care.smith.top.model.CompositeConcept;
import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.model.EntityType;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchSong;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import java.util.List;
import java.util.stream.Collectors;

public class CSVMetadataRecord extends CSVRecordCompositeEntries {
  private static final long serialVersionUID = 1L;

  public static List<String> FIELDS =
      List.of(
          "concept",
          "parents",
          "type",
          "language",
          "titles",
          "synonyms",
          "descriptions",
          "expression");

  public CSVMetadataRecord(
      Concept con, Entity[] concepts, String entryPartsDelimiter, String lang) {
    super(entryPartsDelimiter);
    add(con.getId());
    addParents(con);
    addType(con);
    add(lang);
    addEntry(Entities.getTitles(con));
    addEntry(Entities.getSynonyms(con));
    addEntry(Entities.getDescriptions(con));
    addExpression(con, concepts, lang);
  }

  private void addParents(Concept con) {
    if (con.getSuperConcepts() == null || con.getSuperConcepts().isEmpty()) add("");
    else addEntry(con.getSuperConcepts().stream().map(Concept::getId).collect(Collectors.toList()));
  }

  private void addType(Concept con) {
    if (con.getEntityType() == null) add("");
    else add(con.getEntityType().getValue());
  }

  private void addExpression(Concept con, Entity[] concepts, String lang) {
    if (con.getEntityType() == null || con.getEntityType() == EntityType.SINGLE_CONCEPT) add("");
    else {
      if (con instanceof CompositeConcept) {
        add(
            Expressions.getStringValue(
                ElasticsearchSong.get()
                    .concepts(Entities.of(concepts))
                    .lang(lang)
                    .generate(((CompositeConcept) con).getExpression())));
      } else add("");
    }
  }
}
