package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.DocumentReference;
import care.smith.top.top_document_query.converter.csv.DocumentCSV;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentReferenceTest {
  @Test
  void csvRetainsDuplicateIdsFromDifferentIndicesAndOldColumnPositions() {
    DocumentEntity document = new DocumentEntity().setId("same").setName("name").setText("text");
    List<DocumentHit> hits = List.of(
        new DocumentHit("same", document).setIndex("one").setDataSourceId("source"),
        new DocumentHit("same", document).setIndex("two").setDataSourceId("source"));
    DocumentCSV csv = new DocumentCSV();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    csv.write(hits, output);
    assertEquals(List.of(new DocumentReference("source", "one", "same"),
        new DocumentReference("source", "two", "same")),
        csv.readDocumentReferences(new ByteArrayInputStream(output.toByteArray())));
    assertEquals(List.of("same", "same"), csv.readFirstColumn(new ByteArrayInputStream(output.toByteArray())));
    assertEquals(List.of("NA", "NA"), csv.readColumn(new ByteArrayInputStream(output.toByteArray()), 3));
  }

  @Test
  void legacyCsvCanStillBeReadButCannotInventProvenance() {
    byte[] legacy = "id\tscore\ttitle\toffsets\nid;field\t0.0\tname\t0-2\n".getBytes(StandardCharsets.UTF_8);
    DocumentCSV csv = new DocumentCSV();
    assertEquals(List.of("id"), csv.readFirstColumn(new ByteArrayInputStream(legacy)));
    assertThrows(IllegalArgumentException.class,
        () -> csv.readDocumentReferences(new ByteArrayInputStream(legacy)));
  }

  @Test
  void headerLikeIdsAndNaNamedSourcesRemainValidData() {
    DocumentHit hit = new DocumentHit("index", new DocumentEntity().setName("title").setText("text"))
        .setIndex("one").setDataSourceId("NA");
    DocumentCSV csv = new DocumentCSV();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    csv.write(List.of(hit), output);
    assertEquals(List.of("index"), csv.readFirstColumn(new ByteArrayInputStream(output.toByteArray())));
    assertEquals(List.of(new DocumentReference("NA", "one", "index")),
        csv.readDocumentReferences(new ByteArrayInputStream(output.toByteArray())));
  }

  @Test
  void referenceRequiresAllIdentityComponents() {
    assertThrows(IllegalArgumentException.class, () -> new DocumentReference("source", "", "id"));
    assertThrows(IllegalArgumentException.class, () -> new DocumentReference(null, "one", "id"));
  }
}
