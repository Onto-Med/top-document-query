package care.smith.top.top_phenotypic_query.util.builder.nlp;

import care.smith.top.model.*;
import care.smith.top.top_phenotypic_query.util.Entities;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cat {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Concept c;

  public Cat(String id, String codeSystemUri, boolean composite, String... codes) {
    this(id, composite);
    codes(codeSystemUri, codes);
  }

  public Cat(String id, boolean composite) {
    if (composite) {
      c = (Concept) new CompositeConcept().entityType(EntityType.COMPOSITE_CONCEPT).id(id);
    } else {
      c = (Concept) new SingleConcept().entityType(EntityType.SINGLE_CONCEPT).id(id);
    }
  }

  public static Code getCode(String codeSystemUri, String code) {
    CodeSystem s = new CodeSystem().uri(URI.create(codeSystemUri));
    return new Code().code(code).codeSystem(s);
  }

  public Cat code(String codeSystemUri, String code) {
    c.addCodesItem(getCode(codeSystemUri, code));
    return this;
  }

  public Cat code(String codeUri) {
    String[] sysCode = codeUri.split("\\s*\\|\\s*");
    return code(sysCode[0], sysCode[1]);
  }

  public Cat codes(String codeSystemUri, String... codes) {
    for (String code : codes) code(codeSystemUri, code);
    return this;
  }

  public Cat codes(String... codeUris) {
    for (String codeUri : codeUris) code(codeUri);
    return this;
  }

  public Cat title(String txt) {
    c.addTitlesItem(new LocalisableText().text(txt));
    return this;
  }

  public Cat title(String txt, String lang) {
    c.addTitlesItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Cat titleDe(String txt) {
    return title(txt, "de");
  }

  public Cat titleEn(String txt) {
    return title(txt, "en");
  }

  public Cat synonym(String txt) {
    c.addSynonymsItem(new LocalisableText().text(txt));
    return this;
  }

  public Cat synonym(String txt, String lang) {
    c.addSynonymsItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Cat synonymDe(String txt) {
    return synonym(txt, "de");
  }

  public Cat synonymEn(String txt) {
    return synonym(txt, "en");
  }

  public Cat description(String txt) {
    c.addDescriptionsItem(new LocalisableText().text(txt));
    return this;
  }

  public Cat description(String txt, String lang) {
    c.addDescriptionsItem(new LocalisableText().text(txt).lang(lang));
    return this;
  }

  public Cat descriptionDe(String txt) {
    return description(txt, "de");
  }

  public Cat descriptionEn(String txt) {
    return description(txt, "en");
  }

  public Cat annotations(String txt) {
    Entities.addAnnotations(c, txt);
    return this;
  }

  public Cat expression(Expression exp) {
    if (c instanceof CompositeConcept) {
      ((CompositeConcept) c).setExpression(exp);
    } else {
      log.warn("Tried to set expression for single concept '{}'.", c.getId());
    }
    return this;
  }

  public Cat single() {
    c.setEntityType(EntityType.SINGLE_CONCEPT);
    return this;
  }

  public Cat composite() {
    c.setEntityType(EntityType.COMPOSITE_CONCEPT);
    return this;
  }

  public Cat subCategories(Concept... subConcepts) {
    if (c instanceof SingleConcept) {
      for (Concept subConcept : subConcepts) {
        ((SingleConcept) c).addSubConceptsItem(subConcept);
      }
    } else {
      log.warn("Tried to add sub concepts to composite concept '{}'.", c.getId());
    }
    return this;
  }

  public Concept get() {
    return c;
  }
}
