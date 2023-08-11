package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Concept;
import care.smith.top.top_document_query.adapter.ElasticDocument;
import care.smith.top.top_document_query.adapter.lucene.LuceneSong;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.builder.Cat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

//ToDo: Test still uses local ES instance and not the TestContainer
class LuceneAdapterTest extends AbstractElasticTest {
  Concept documentEntity = new Cat("document", false).titleEn("document").get();
  Concept entityEntity = new Cat("entity", false).titleEn("entity").synonymEn("entities").get();

  @BeforeAll
  static void setUp() {
    setUpESIndex();
  }

  @Test
  void testAdapterConnection() throws InstantiationException {
    initAdaper();
  }

  @Test
  void testExecute1() throws InstantiationException {
    Entities concepts = Entities.of(documentEntity, entityEntity);
    String queryString =
        Expressions.getStringValue(
            LuceneSong.get()
                .concepts(concepts)
                .lang("en")
                .generate(And.of(documentEntity, entityEntity)));
    int correctDocumentCount =
        2; // "test01": has "document" & "entity"; "test02": "document" & "entities"
    // not "test03": has "document" but neither "entity" nor "entities" ((see setUp))

    initAdaper();
    assertNotNull(adapter);

    List<ElasticDocument> documents = adapter.execute(queryString);
    assertEquals(correctDocumentCount, documents.size());
    assertEquals(
        new HashSet<>(Arrays.asList("test01", "test02")),
        documents.stream().map(ElasticDocument::getName).collect(Collectors.toSet()));

    // Test if "field" specification correctly excludes documents when searching on "name" field
    adapter.getConfig().setField(new String[] {"name"});
    documents = adapter.execute(queryString);
    assertNotEquals(correctDocumentCount, documents.size());
  }
}
