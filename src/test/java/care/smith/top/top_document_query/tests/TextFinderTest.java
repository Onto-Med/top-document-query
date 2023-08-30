package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Concept;
import care.smith.top.top_document_query.adapter.AbstractDocument;
import care.smith.top.top_document_query.adapter.TextFinder;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.builder.Exp;
import care.smith.top.top_document_query.util.builder.CQue;
import care.smith.top.top_document_query.util.builder.Cat;
import java.util.List;
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
  Entities entities = Entities.of(parentCat, phrase1, phrase2);

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
        new CQue(
                adapter,
                adapter.getConfig(),
                entities.getConcepts().toArray(new Concept[0]),
                PARENT_CAT_ID,
                "en")
            .getFinder();
    List<AbstractDocument> documents = tf.execute();
    assertEquals(1, documents.size());
  }
}
