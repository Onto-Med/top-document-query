package care.smith.top.top_document_query.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Concept;
import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.elasticsearch.ElasticsearchSong;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import javax.print.Doc;

class ElasticsearchAdapterTest extends AbstractElasticTest {
  Concept documentEntity = new Cat("document", false).titleEn("document").get();
  Concept entityEntity = new Cat("entity", false).titleEn("entity").synonymEn("entities").get();

  @BeforeAll
  static void setUp() throws InstantiationException {
    setUpESIndex();
//    setUpLocalESIndex();
    initAdaper();
//    initLocalAdaper();
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
    adapter
        .getAllDocumentsBatched(1)
        .forEach(
            result -> {
              assertEquals(1, result.size());
              count.getAndIncrement();
            });
    assertEquals(3, count.get());
  }

  @Test
  void getAllDocuments() throws IOException {
    assertEquals(
        allTestDocuments.stream().map(d -> new Document().id(d.getId()).name(d.getName()).text(d.getText())).collect(Collectors.toSet()),
        adapter.getAllDocuments(null).toSet());
  }

  @Test
  void getDocumentById() throws IOException {
    assertEquals(
        document1,
        adapter.getDocumentById("d1").orElseThrow());
    assertEquals(
        document2,
        adapter.getDocumentById("d2").orElseThrow());
    assertEquals(
        document3,
        adapter.getDocumentById("d3").orElseThrow());
  }

  @Test
  void getDocumentsByIds() throws IOException {
    Page<Document> result1 = adapter.getDocumentsByIds(List.of("d2"), null);
    assertNotNull(result1.getContent());
    assertEquals(
        allTestDocuments,
        adapter.getDocumentsByIds(List.of("d1", "d2", "d3")).collect(Collectors.toSet()));

    assertEquals(document2, result1.getContent().get(0));
    assertEquals(
        allTestDocuments,
        adapter.getDocumentsByIds(List.of("d1", "d2", "d3"), null).toSet());

  }

  @Test
  void getDocumentsByName() throws IOException {
    Page<Document> result1 = adapter.getDocumentsByName("test01", null);
    Page<Document> result2 = adapter.getDocumentsByName("test", null);

    assertNotNull(result1.getContent());

    assertEquals(document1, result1.getContent().get(0));
    assertEquals(allTestDocuments, result2.toSet());
  }

  @Test
  void getDocumentsByTerms() throws IOException {
    Page<Document> result1 = adapter.getDocumentsByTerms(List.of("document", "here"), TermConcatenationTypes.AND, null);
    Page<Document> result2 = adapter.getDocumentsByTerms(List.of("entity", "entities", "third"), TermConcatenationTypes.OR, null);
    Page<Document> result3 = adapter.getDocumentsByTerms(List.of("document", "features", "but"), null);

    assertEquals(Set.of(document1, document2), result1.toSet());
    assertEquals(allTestDocuments, result2.toSet());
    assertNotNull(result3.getContent());
    assertEquals(document3, result3.getContent().get(0));
  }

  @Test
  void getDocumentsByIdsAndTerms() throws IOException {
    Page<Document> result1 = adapter.getDocumentsByIdsAndTerms(List.of("d1", "d2", "d3"), List.of("test", "document"), TermConcatenationTypes.AND, null);
    Page<Document> result2 = adapter.getDocumentsByIdsAndTerms(List.of("d1", "d2", "d3"), List.of("test", "document"), TermConcatenationTypes.OR, null);
    Page<Document> result3 = adapter.getDocumentsByIdsAndTerms(List.of("d1", "d2"), List.of("test", "document"), TermConcatenationTypes.OR, null);

    assertEquals(Set.of(document1, document3), result1.toSet());
    assertEquals(allTestDocuments, result2.toSet());
    assertEquals(Set.of(document1, document2), result3.toSet());
  }
}
