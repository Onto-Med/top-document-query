package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.TextFinder;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.util.builder.CQue;
import care.smith.top.top_document_query.util.builder.Cat;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TextFinderTest extends AbstractElasticTest {
  final String PARENT_CAT_ID = "phrase_search_cat";
  Concept phrase1 = new Cat("phrase1", false).titleEn("\"a document\"").get();
  Concept phrase2 = new Cat("phrase2", false).titleEn("entity").get();
  Concept parentCat =
      new Cat(PARENT_CAT_ID, true)
          .titleEn("phrase_search_cat")
          .expression(And.of(Dist.of(phrase1, 1), Exp.of(phrase2)))
          .get();
  Map<String, Set<String>> dependencies = new HashMap<>();
  Map<String, Entity> concepts =
      Map.of("phrase1", phrase1, "phrase2", phrase2, PARENT_CAT_ID, parentCat);

  @BeforeAll
  static void setUp() {
    setUpESIndex();
  }

  @Test
  void getEntities() {}

  @Test
  void execute() throws InstantiationException {
    initAdaper();
    TextFinder tf =
        new CQue(adapter, adapter.getConfig(), concepts, dependencies, PARENT_CAT_ID, "en")
            .getFinder();
    List<DocumentHit> documents = tf.execute().flatMap(List::stream).toList();
    assertEquals(1, documents.size());
  }
}
