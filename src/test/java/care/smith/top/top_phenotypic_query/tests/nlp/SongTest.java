package care.smith.top.top_phenotypic_query.tests.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Concept;
import care.smith.top.model.Expression;
import care.smith.top.top_phenotypic_query.song.adapter.lucene.LuceneSong;
import care.smith.top.top_phenotypic_query.song.functions.And;
import care.smith.top.top_phenotypic_query.song.functions.Dist;
import care.smith.top.top_phenotypic_query.song.functions.Not;
import care.smith.top.top_phenotypic_query.song.functions.Or;
import care.smith.top.top_phenotypic_query.song.functions.SubTree;
import care.smith.top.top_phenotypic_query.song.functions.XProd;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.nlp.Cat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SongTest {

  private final Logger log = LoggerFactory.getLogger(SongTest.class);

  Concept a =
      new Cat("a", false)
          .titleDe("a- de")
          .titleEn("a- en")
          .synonymDe("a1- de")
          .synonymEn("a1- en")
          .synonymDe("a2- de")
          .synonymEn("a2- en")
          .get();

  Concept b =
      new Cat("b", false)
          .titleDe("b-de")
          .titleEn("b-en")
          .synonymDe("b1-de")
          .synonymEn("b1-en")
          .synonymDe("b2-de")
          .synonymEn("b2-en")
          .get();

  Concept e =
      new Cat("e", false)
          .titleDe("e-de")
          .titleEn("e-en")
          .synonymDe("e1-de")
          .synonymEn("e1-en")
          .synonymDe("e2- de")
          .synonymEn("e2-en")
          .get();

  Concept d =
      new Cat("d", false)
          .titleDe("d-de")
          .titleEn("d-en")
          .synonymDe("d1-de")
          .synonymEn("d1-en")
          .synonymDe("d2-de")
          .synonymEn("d2-en")
          .subCategories(e)
          .get();

  Concept c =
      new Cat("c", false)
          .titleDe("c-de")
          .titleEn("c-en")
          .synonymDe("c1-de")
          .synonymEn("c1-en")
          .synonymDe("c2-de")
          .synonymEn("c2-en")
          .subCategories(d)
          .get();

  Concept f =
      new Cat("f", false)
          .titleDe("f-de")
          .titleEn("f-en")
          .synonymDe("f1-de")
          .synonymEn("f1-en")
          .synonymDe("f2- de")
          .synonymEn("f2-en")
          .get();

  Entities concepts = Entities.of(a, b, c, d, e, f);

  @Test
  public void test1() {
    log.debug("===== Test 1 =====");
    Expression exp = And.of(Or.of(a, b), Not.of(c));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "(((\"a- de\" OR \"a1- de\" OR \"a2- de\") OR (b-de OR b1-de OR b2-de)) AND NOT (c-de OR"
            + " c1-de OR c2-de))",
        query);
  }

  @Test
  public void test2() {
    log.debug("===== Test 2 =====");
    Expression exp = And.of(Or.of(a, b), Not.of(SubTree.of(c)));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "(((\"a- de\" OR \"a1- de\" OR \"a2- de\") OR (b-de OR b1-de OR b2-de)) AND NOT (c-de OR"
            + " c1-de OR c2-de OR d-de OR d1-de OR d2-de OR e-de OR e1-de OR \"e2- de\"))",
        query);
  }

  @Test
  public void test3() {
    log.debug("===== Test 3 =====");
    Expression exp = And.of(Or.of(Dist.of(a, 5), Exp.of(b)), Not.of(c));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "(((\"a- de\"~5 OR \"a1- de\"~5 OR \"a2- de\"~5) OR (b-de OR b1-de OR b2-de)) AND NOT (c-de"
            + " OR c1-de OR c2-de))",
        query);
  }

  @Test
  public void test4() {
    log.debug("===== Test 4 =====");
    Expression exp = And.of(XProd.of(a, b), Not.of(c));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "((\"a- de b-de\" OR \"a- de b1-de\" OR \"a- de b2-de\" OR \"a1- de b-de\" OR \"a1- de"
            + " b1-de\" OR \"a1- de b2-de\" OR \"a2- de b-de\" OR \"a2- de b1-de\" OR \"a2- de"
            + " b2-de\") AND NOT (c-de OR c1-de OR c2-de))",
        query);
  }

  @Test
  public void test5() {
    log.debug("===== Test 5 =====");
    Expression exp = And.of(Dist.of(XProd.of(a, b), 2), Not.of(c));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "((\"a- de b-de\"~2 OR \"a- de b1-de\"~2 OR \"a- de b2-de\"~2 OR \"a1- de b-de\"~2 OR \"a1-"
            + " de b1-de\"~2 OR \"a1- de b2-de\"~2 OR \"a2- de b-de\"~2 OR \"a2- de b1-de\"~2 OR"
            + " \"a2- de b2-de\"~2) AND NOT (c-de OR c1-de OR c2-de))",
        query);
  }

  @Test
  public void test6() {
    log.debug("===== Test 6 =====");
    Expression exp = And.of(Or.of(a, b), Not.of(And.of(c, f)));
    String query =
        Expressions.getStringValue(LuceneSong.get().concepts(concepts).lang("de").generate(exp));
    assertEquals(
        "(((\"a- de\" OR \"a1- de\" OR \"a2- de\") OR (b-de OR b1-de OR b2-de)) AND NOT ((c-de OR"
            + " c1-de OR c2-de) AND (f-de OR f1-de OR \"f2- de\")))",
        query);
  }
}
