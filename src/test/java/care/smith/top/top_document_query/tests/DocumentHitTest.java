package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.converter.csv.CSVDataRecord;
import care.smith.top.top_document_query.converter.csv.DocumentCSV;
import care.smith.top.top_document_query.elasticsearch.DocumentEntity;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DocumentHitTest {

  static Map<String, List<Pair<Integer, Integer>>> testOffsets;
  static Map<String, List<String>> testHighlights;
  static DocumentHit documentHit;
  static final String fieldName = "text";
  static final String documentId = "documentId";
  static final String documentName = "documentName";
  static final Double documentScore = 0.5;

  @BeforeAll
  static void setUpBeforeClass() {
    testOffsets =
        Map.of(
            fieldName,
            List.of(
                Pair.of(134, 141),
                Pair.of(162, 168),
                Pair.of(231, 239),
                Pair.of(244, 250),
                Pair.of(295, 309)));
    testHighlights =
        Map.of(
            fieldName,
            List.of(
                "Opiate: (Heroin, Morphium, Metadon, Opium, Subutex, Tilidin, Tramadol, Codein) sei seit Dezember 2016 clean,\n vorher regelmäßig 32 mg <em>Subutex</em> nasal, immer wieder <em>Heroin</em> und gelegentlich Kokain.\n Morphium, Methadon, Opium, Tilidin, <em>Tramadol</em> und <em>Codein</em> war nur eine Nebensache).\n•\tAmphetamine und <em>Benzodiazepine</em> nur Selten."));
    DocumentEntity documentEntity = new DocumentEntity().setId(documentId).setName(documentName);
    documentHit = new DocumentHit(documentId, documentEntity, testHighlights, documentScore);
  }

  @Test
  void offsetGeneration() {
    assertEquals(testOffsets, documentHit.getHighlights());
  }

  @Test
  void csvWrite() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    DocumentCSV csv = new DocumentCSV();
    csv.write(List.of(documentHit), out);

    List<String> outPut = out.toString().lines().toList();
    // DOCUMENTID;FIELDNAME‖SCORE‖Title‖Begin1-End1;Begin2-End2
    String expected =
        String.join(
            DocumentCSV.entriesDelimiter,
            List.of(
                String.format("%s%s%s", documentId, DocumentCSV.entryPartsDelimiter, fieldName),
                String.valueOf(documentScore),
                documentName,
                testOffsets.get(fieldName).stream()
                    .map(s -> String.format("%s-%s", s.getLeft(), s.getRight()))
                    .collect(Collectors.joining(DocumentCSV.entryPartsDelimiter))));
    assertEquals(expected, outPut.get(outPut.size() - 1));
  }

  @Test
  void csvGetIds() {
    String header = String.join(DocumentCSV.entriesDelimiter, CSVDataRecord.FIELDS);
    String row1 =
        String.join(
            DocumentCSV.entriesDelimiter,
            List.of(
                "id1",
                "0.1",
                "doc1",
                String.join(DocumentCSV.entryPartsDelimiter, List.of("0-1", "4-10"))));
    String row2 =
        String.join(
            DocumentCSV.entriesDelimiter,
            List.of(
                "id2;field1",
                "0.5",
                "doc2",
                String.join(DocumentCSV.entryPartsDelimiter, List.of("0-1", "4-10"))));
    String row3 =
        String.join(
            DocumentCSV.entriesDelimiter,
            List.of(
                "id2;field2",
                "0.4",
                "doc2",
                String.join(DocumentCSV.entryPartsDelimiter, List.of("2-4", "10-15"))));
    String row4 = String.join(DocumentCSV.entriesDelimiter, List.of("id3", "0.2", "doc3", "NA"));
    Set<String> expectedIds = Set.of("id1", "id2", "id3");

    InputStream in =
        new ByteArrayInputStream(
            String.join("\n", List.of(header, row1, row2, row3, row4)).getBytes());
    DocumentCSV csv = new DocumentCSV();

    assertEquals(expectedIds, Set.copyOf(csv.readFirstColumn(in)));
  }
}
