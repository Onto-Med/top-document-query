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
  final String entriesDelimiter = "\t";
  final String entryPartsDelimiter = ";";
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
                Pair.of(133, 140),
                Pair.of(161, 167),
                Pair.of(229, 237),
                Pair.of(242, 248),
                Pair.of(291, 305)));
    testHighlights =
        Map.of(
            fieldName,
            List.of(
                "Opiate: (Heroin, Morphium, Metadon, Opium, Subutex, Tilidin, Tramadol, Codein) sei seit Dezember 2016 clean, vorher regelmäßig 32 mg <em>Subutex</em> nasal, immer wieder <em>Heroin</em> und gelegentlich Kokain. Morphium, Methadon, Opium, Tilidin, <em>Tramadol</em> und <em>Codein</em> war nur eine Nebensache). Amphetamine und <em>Benzodiazepine</em> nur Selten."));
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
    csv.entriesDelimiter(entriesDelimiter);
    csv.entryPartsDelimiter(entryPartsDelimiter);
    csv.write(List.of(documentHit), out);

    List<String> outPut = out.toString().lines().toList();
    // DOCUMENTID;FIELDNAME‖SCORE‖Title‖Begin1-End1;Begin2-End2
    String expected =
        String.join(
            entriesDelimiter,
            List.of(
                String.format("%s%s%s", documentId, entryPartsDelimiter, fieldName),
                String.valueOf(documentScore),
                documentName,
                testOffsets.get(fieldName).stream()
                    .map(s -> String.format("%s-%s", s.getLeft(), s.getRight()))
                    .collect(Collectors.joining(entryPartsDelimiter))));
    assertEquals(expected, outPut.get(outPut.size() - 1));
  }

  @Test
  void csvGetIds() {
    String header = String.join(entriesDelimiter, CSVDataRecord.FIELDS);
    String row1 =
        String.join(
            entriesDelimiter,
            List.of(
                "id1", "0.1", "doc1", String.join(entryPartsDelimiter, List.of("0-1", "4-10"))));
    String row2 =
        String.join(
            entriesDelimiter,
            List.of(
                "id2;field1",
                "0.5",
                "doc2",
                String.join(entryPartsDelimiter, List.of("0-1", "4-10"))));
    String row3 =
        String.join(
            entriesDelimiter,
            List.of(
                "id2;field2",
                "0.4",
                "doc2",
                String.join(entryPartsDelimiter, List.of("2-4", "10-15"))));
    String row4 = String.join(entriesDelimiter, List.of("id3", "0.2", "doc3", "NA"));
    Set<String> expectedIds = Set.of("id1", "id2", "id3");

    InputStream in =
        new ByteArrayInputStream(
            String.join("\n", List.of(header, row1, row2, row3, row4)).getBytes());
    DocumentCSV csv = new DocumentCSV();
    csv.entriesDelimiter(entriesDelimiter);
    csv.entryPartsDelimiter(entryPartsDelimiter);

    assertEquals(expectedIds, Set.copyOf(csv.readFirstColumn(in)));
  }
}
