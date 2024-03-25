package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Concept;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchSong;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.builder.Cat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElasticsearchAdapterTest extends AbstractElasticTest {
  Concept documentEntity = new Cat("document", false).titleEn("document").get();
  Concept entityEntity = new Cat("entity", false).titleEn("entity").synonymEn("entities").get();

  @BeforeAll
  static void setUp() {
    setUpESIndex();
  }

  @BeforeEach
  void initAdapter() throws InstantiationException {
    initAdaper();
    assertNotNull(adapter);
  }

  @Test
  void count() {
    assertEquals(
        Long.valueOf(3), adapter.count());
  }

  @Test
  void testExecute1() {
    Entities concepts = Entities.of(documentEntity, entityEntity);
    String queryString =
        Expressions.getStringValue(
            ElasticsearchSong.get()
                .concepts(concepts)
                .lang("en")
                .generate(And.of(documentEntity, entityEntity)));
    // "test01": has "document" & "entity"; "test02": "document" & "entities"
    // not "test03": has "document" but neither "entity" nor "entities" ((see setUp))
    int correctDocumentCount = 2;

    List<DocumentHit> documents = adapter.execute(queryString);
    assertEquals(correctDocumentCount, documents.size());
    assertEquals(
        new HashSet<>(Arrays.asList("test01", "test02")),
        documents.stream()
            .map(documentHit -> documentHit.getDocument().getName())
            .collect(Collectors.toSet()));

    // Test if "field" specification correctly excludes documents when searching on "name" field
    adapter.getConfig().setField(new String[] {"name"});
    documents = adapter.execute(queryString);
    assertNotEquals(correctDocumentCount, documents.size());
  }

  @Test
  void getAllDocumentsBatched() {
    AtomicInteger count = new AtomicInteger();
    adapter.getAllDocumentsBatched(1).forEach(
        result -> {
          assertEquals(1, result.size());
          count.getAndIncrement();
        }
    );
    assertEquals(3, count.get());
  }

  @Test
  void getAllDocuments() throws IOException {
    assertEquals(
        allTestDocuments,
        adapter.getAllDocuments(null).toSet());
  }

  @Test
  void getDocumentById() {
  }

  @Test
  void getDocumentsByName() {
  }

  @Test
  void getDocumentsByIds() {
  }

  @Test
  void getDocumentsByTerms() {
  }

  @Test
  void getDocumentsByIdsAndTerms() {
  }
}
