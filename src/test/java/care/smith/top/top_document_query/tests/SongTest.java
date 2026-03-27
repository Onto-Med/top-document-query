package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Concept;
import care.smith.top.model.Expression;
import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchSong;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.functions.SubTree;
import care.smith.top.top_document_query.functions.XProd;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.builder.Cat;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.Map;
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

  Concept de_only = new Cat("de_only", false).titleDe("de_only").synonymDe("de_only_syn").get();

  Concept en_and_de =
      new Cat("en_and_de", false)
          .titleDe("de_title")
          .synonymDe("de_syn")
          .titleEn("en_title")
          .synonymEn("en_syn")
          .get();

  Entities concepts = Entities.of(a, b, c, d, e, f);
  Entities concepts_for_lang = Entities.of(de_only, en_and_de);

  @Test
  public void test1() {
    log.debug("===== Test 1 =====");
    Expression exp = And.of(Or.of(a, b), Not.of(c));
    String query =
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp));
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
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp));
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
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp));
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
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp));
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
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp));
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
    Expression genExp = ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp);
    String query = Expressions.getStringValue(genExp);
    assertEquals(
        "(((\"a- de\" OR \"a1- de\" OR \"a2- de\") OR (b-de OR b1-de OR b2-de)) AND NOT ((c-de OR"
            + " c1-de OR c2-de) AND (f-de OR f1-de OR \"f2- de\")))",
        query);
  }

  @Test
  public void test7() {
    log.debug("===== Test 7 =====");
    Expression exp1 = SubTree.of(c, 1);
    Expression genExp1 = ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp1);
    String query1 = Expressions.getStringValue(genExp1);
    assertEquals("(c-de OR c1-de OR c2-de OR d-de OR d1-de OR d2-de)", query1);

    Expression exp2 = SubTree.of(c, 0);
    Expression genExp2 = ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp2);
    String query2 = Expressions.getStringValue(genExp2);
    assertEquals("(c-de OR c1-de OR c2-de)", query2);

    Expression exp3 = SubTree.of(c, -1);
    Expression genExp3 = ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp3);
    String query3 = Expressions.getStringValue(genExp3);
    assertEquals(
        "(c-de OR c1-de OR c2-de OR d-de OR d1-de OR d2-de OR e-de OR e1-de OR \"e2- de\")",
        query3);
  }

  @Test
  public void test_distance() {
    log.debug("===== Test Distance =====");
    Expression exp0 = Dist.of(Dist.of(b, 5), 2);
    String query0 = ElasticsearchSong.get().getQuery(ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp0));
    assertEquals(
"(b-de~5 OR b1-de~5 OR b2-de~5)", query0
    );
    Expression exp1 = Not.of(Dist.of(XProd.of(a, b), 2));
    String query1 = ElasticsearchSong.get().getQuery(ElasticsearchSong.get().concepts(concepts).lang("de").generate(exp1));
    assertEquals(
"NOT (\"a- de b-de\"~2 OR \"a- de b1-de\"~2 OR \"a- de b2-de\"~2 OR \"a1- de b-de\"~2 OR"
       + " \"a1- de b1-de\"~2 OR \"a1- de b2-de\"~2 OR \"a2- de b-de\"~2 OR \"a2- de b1-de\"~2 OR"
       + " \"a2- de b2-de\"~2)", query1
    );
  }

  @Test
  public void test_no_set_language() {
    // if no language is set, it should take all languages
    Expression exp = And.of(de_only, en_and_de);
    String query =
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts_for_lang).lang(null).generate(exp));
    assertEquals(
        "((de_only OR de_only_syn) AND (de_title OR en_title OR de_syn OR en_syn))", query);
  }

  @Test
  public void test_set_specific_language_accessible() {
    Expression exp = And.of(de_only, en_and_de);
    String query =
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts_for_lang).lang("de").generate(exp));
    assertEquals("((de_only OR de_only_syn) AND (de_title OR de_syn))", query);
  }

  @Test
  public void test_set_specific_language_not_accessible() {
    Expression exp = And.of(de_only, en_and_de);
    String query =
        Expressions.getStringValue(
            ElasticsearchSong.get().concepts(concepts_for_lang).lang("en").generate(exp));
    assertEquals("((en_title OR en_syn))", query);
  }

  @Test
  public void test_get_subdepth() {
    Expression exp1 = And.of(c, d);
    Map<String, Integer> map1 =
        ElasticsearchSong.get().concepts(concepts).checkForSubconceptResolution(exp1);
    assertEquals(0, map1.size());

    Expression exp2 = And.of(Or.of(a, b), Not.of(SubTree.of(c)));
    Map<String, Integer> map2 =
        ElasticsearchSong.get().concepts(concepts).checkForSubconceptResolution(exp2);
    assertEquals(1, map2.size());

    Expression exp3 = And.of(SubTree.of(a, 1), Not.of(SubTree.of(c)));
    Map<String, Integer> map3 =
        ElasticsearchSong.get().concepts(concepts).checkForSubconceptResolution(exp3);
    assertEquals(2, map3.size());
    assertEquals(1, map3.get("a"));
    assertEquals(-1, map3.get("c"));
  }
}
