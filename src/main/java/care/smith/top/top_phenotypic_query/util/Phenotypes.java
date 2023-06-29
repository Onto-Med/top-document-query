package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.Code;
import care.smith.top.model.DataType;
import care.smith.top.model.Entity;
import care.smith.top.model.EntityType;
import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phenotypes {

  public static boolean isSinglePhenotype(Phenotype p) {
    return p.getEntityType() == EntityType.SINGLE_PHENOTYPE;
  }

  public static boolean isSingleRestriction(Phenotype p) {
    return p.getEntityType() == EntityType.SINGLE_RESTRICTION;
  }

  public static boolean isCompositePhenotype(Phenotype p) {
    return p.getEntityType() == EntityType.COMPOSITE_PHENOTYPE;
  }

  public static boolean isCompositeRestriction(Phenotype p) {
    return p.getEntityType() == EntityType.COMPOSITE_RESTRICTION;
  }

  public static boolean isSingle(Phenotype p) {
    return isSinglePhenotype(p) || isSingleRestriction(p);
  }

  public static boolean isComposite(Phenotype p) {
    return isCompositePhenotype(p) || isCompositeRestriction(p);
  }

  public static boolean isPhenotype(Phenotype p) {
    return isSinglePhenotype(p) || isCompositePhenotype(p);
  }

  public static boolean isRestriction(Phenotype p) {
    return isSingleRestriction(p) || isCompositeRestriction(p);
  }

  public static boolean isCategory(Entity e) {
    return e.getEntityType() == EntityType.CATEGORY;
  }

  public static String getUnrestrictedPhenotypeId(Phenotype p) {
    return (isRestriction(p)) ? p.getSuperPhenotype().getId() : p.getId();
  }

  public static String getCodeUri(Code code) {
    return code.getCodeSystem().getUri().toString() + "|" + code.getCode();
  }

  public static List<Code> getUnrestrictedPhenotypeCodes(Phenotype p) {
    return (isRestriction(p)) ? p.getSuperPhenotype().getCodes() : p.getCodes();
  }

  public static List<String> getUnrestrictedPhenotypeCodes(Phenotype p, String codeSystem) {
    return getUnrestrictedPhenotypeCodes(p).stream()
        .filter(c -> Objects.equals(c.getCodeSystem().getUri().toString(), codeSystem))
        .map(c -> c.getCode())
        .collect(Collectors.toList());
  }

  public static Stream<String> getUnrestrictedPhenotypeCodeUris(Phenotype p) {
    return getUnrestrictedPhenotypeCodes(p).stream().map(c -> getCodeUri(c));
  }

  public static List<String> getCodeUris(Phenotype p) {
    if (p.getCodes() == null) return new ArrayList<>();
    return p.getCodes().stream().map(c -> getCodeUri(c)).collect(Collectors.toList());
  }

  public static ItemType getItemType(Phenotype p) {
    return (isRestriction(p)) ? p.getSuperPhenotype().getItemType() : p.getItemType();
  }

  public static boolean hasExistentialQuantifier(Phenotype p) {
    Restriction r = p.getRestriction();
    if (r == null) return false;
    return Restrictions.hasExistentialQuantifier(r);
  }

  public static DataType getDataType(Phenotype p) {
    return (isRestriction(p)) ? p.getSuperPhenotype().getDataType() : p.getDataType();
  }

  public static boolean hasStringType(Phenotype p) {
    return getDataType(p) == DataType.STRING;
  }

  public static boolean hasNumberType(Phenotype p) {
    return getDataType(p) == DataType.NUMBER;
  }

  public static boolean hasBooleanType(Phenotype p) {
    return getDataType(p) == DataType.BOOLEAN;
  }

  public static boolean hasDateTimeType(Phenotype p) {
    return getDataType(p) == DataType.DATE_TIME;
  }

  public static Phenotype clone(Phenotype p) {
    return (Phenotype)
        new Phenotype()
            .dataType(p.getDataType())
            .expression(p.getExpression())
            .itemType(p.getItemType())
            .restriction(p.getRestriction())
            .superPhenotype(p.getSuperPhenotype())
            .unit(p.getUnit())
            .author(p.getAuthor())
            .codes(p.getCodes())
            .createdAt(p.getCreatedAt())
            .descriptions(p.getDescriptions())
            .entityType(p.getEntityType())
            .equivalentEntities(p.getEquivalentEntities())
            .id(p.getId())
            .refer(p.getRefer())
            .repository(p.getRepository())
            .synonyms(p.getSynonyms())
            .titles(p.getTitles())
            .version(p.getVersion());
  }

  public static String getRestrictedValuesKey(Phenotype singleRestriction) {
    return singleRestriction.getSuperPhenotype().getId() + "_values_" + singleRestriction.getId();
  }

  public static boolean isRestrictedValuesKeyOfPhenotype(String key, String singlePhenotypeId) {
    return key.startsWith(singlePhenotypeId + "_values_");
  }
}
