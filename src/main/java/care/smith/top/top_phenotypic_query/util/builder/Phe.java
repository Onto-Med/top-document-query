package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.Code;
import care.smith.top.model.CodeSystem;
import care.smith.top.model.DataType;
import care.smith.top.model.EntityType;
import care.smith.top.model.Expression;
import care.smith.top.model.ItemType;
import care.smith.top.model.LocalisableText;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import java.net.URI;

public class Phe {

  private Phenotype p;

  public Phe(String id, String codeSystemUri, String... codes) {
    this(id);
    codes(codeSystemUri, codes);
  }

  public Phe(String id) {
    p = (Phenotype) new Phenotype().itemType(ItemType.OBSERVATION).id(id);
  }

  public Phe itemType(ItemType itemType) {
    p.itemType(itemType);
    return this;
  }

  public static Code getCode(String codeSystemUri, String code) {
    CodeSystem s = new CodeSystem().uri(URI.create(codeSystemUri));
    return new Code().code(code).codeSystem(s);
  }

  public Phe code(String codeSystemUri, String code) {
    p.addCodesItem(getCode(codeSystemUri, code));
    return this;
  }

  public Phe code(String codeUri) {
    String[] sysCode = codeUri.split("\\s*\\|\\s*");
    return code(sysCode[0], sysCode[1]);
  }

  public Phe codes(String codeSystemUri, String... codes) {
    for (String code : codes) code(codeSystemUri, code);
    return this;
  }

  public Phe codes(String... codeUris) {
    for (String codeUri : codeUris) code(codeUri);
    return this;
  }

  public Phe title(String txt) {
    p.addTitlesItem(new LocalisableText().text(txt));
    return this;
  }

  public Phe title(String txt, String lang) {
    p.addTitlesItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Phe titleDe(String txt) {
    return title(txt, "de");
  }

  public Phe titleEn(String txt) {
    return title(txt, "en");
  }

  public Phe synonym(String txt) {
    p.addSynonymsItem(new LocalisableText().text(txt));
    return this;
  }

  public Phe synonym(String txt, String lang) {
    p.addSynonymsItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Phe synonymDe(String txt) {
    return synonym(txt, "de");
  }

  public Phe synonymEn(String txt) {
    return synonym(txt, "en");
  }

  public Phe description(String txt) {
    p.addDescriptionsItem(new LocalisableText().text(txt));
    return this;
  }

  public Phe description(String txt, String lang) {
    p.addDescriptionsItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Phe descriptionDe(String txt) {
    return description(txt, "de");
  }

  public Phe descriptionEn(String txt) {
    return description(txt, "en");
  }

  public Phe annotations(String txt) {
    Entities.addAnnotations(p, txt);
    return this;
  }

  public Phe dataType(String dt) {
    return dataType(DataType.fromValue(dt));
  }

  public Phe dataType(DataType dt) {
    p.dataType(dt).entityType(EntityType.SINGLE_PHENOTYPE);
    return this;
  }

  public Phe string() {
    return dataType(DataType.STRING);
  }

  public Phe number() {
    return dataType(DataType.NUMBER);
  }

  public Phe number(String unit) {
    p.unit(unit);
    return number();
  }

  public Phe dateTime() {
    return dataType(DataType.DATE_TIME);
  }

  public Phe bool() {
    return dataType(DataType.BOOLEAN);
  }

  public Phe restriction(Phenotype superPhenotype, Restriction r) {
    p.superPhenotype(superPhenotype).restriction(r).dataType(DataType.BOOLEAN);
    if (Phenotypes.isSinglePhenotype(superPhenotype)) p.entityType(EntityType.SINGLE_RESTRICTION);
    else p.entityType(EntityType.COMPOSITE_RESTRICTION);
    return this;
  }

  public Phe expression(Expression e) {
    p.expression(e).entityType(EntityType.COMPOSITE_PHENOTYPE);
    return this;
  }

  public Phenotype get() {
    return p;
  }
}
