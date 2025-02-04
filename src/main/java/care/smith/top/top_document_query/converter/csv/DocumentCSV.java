package care.smith.top.top_document_query.converter.csv;

import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.util.Entities;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;

public class DocumentCSV {

  private Charset charset = StandardCharsets.UTF_8;
  private String entriesDelimiter = "\t";
  private String entryPartsDelimiter = ";";
  private String language = null;
  private int excerptLength = 100;
  private final String nullValueString = "NA";

  public DocumentCSV() {}

  public DocumentCSV excerptLength(int excerptLength) {
    this.excerptLength = excerptLength;
    return this;
  }

  public DocumentCSV language(String language) {
    this.language = language;
    return this;
  }

  public DocumentCSV charset(Charset charset) {
    this.charset = charset;
    return this;
  }

  public DocumentCSV charset(String charset) {
    return charset(Charset.forName(charset));
  }

  public DocumentCSV entriesDelimiter(String entriesDelimiter) {
    this.entriesDelimiter = entriesDelimiter;
    return this;
  }

  public DocumentCSV entryPartsDelimiter(String entryPartsDelimiter) {
    this.entryPartsDelimiter = entryPartsDelimiter;
    return this;
  }

  public void write(Entity[] concepts, OutputStream outputStream) {
    CSVWriter writer = new CSVWriter(outputStream, entriesDelimiter, charset);
    writer.write(CSVMetadataRecord.FIELDS);
    for (Concept con : Entities.of(concepts).getConcepts())
      writer.write(new CSVMetadataRecord(con, concepts, entryPartsDelimiter, language));
    writer.flush();
  }

  public void write(List<DocumentHit> documents, OutputStream outputStream) {
    CSVWriter writer = new CSVWriter(outputStream, entriesDelimiter, charset);
    writer.write(CSVDataRecord.FIELDS);
    for (DocumentHit document : documents) {
      Map<String, List<Pair<Integer, Integer>>> highlights = document.getHighlights();
      // DocumentID;FIELDNAME ‖ Score ‖ Title ‖ Begin1-End1;Begin2-End2
      if (highlights == null || highlights.isEmpty()) {
        writer.write(
            new CSVDataRecord(
                document.getDocumentId(),
                String.valueOf(document.getScore()),
                document.getDocument().getName(),
                nullValueString));
      } else {
        for (Map.Entry<String, List<Pair<Integer, Integer>>> hl : highlights.entrySet()) {
          writer.write(
              new CSVDataRecord(
                  String.format(
                      "%s%s%s", document.getDocumentId(), entryPartsDelimiter, hl.getKey()),
                  String.valueOf(document.getScore()),
                  document.getDocument().getName(),
                  String.join(
                      entryPartsDelimiter,
                      hl.getValue().stream()
                          .map(p -> String.format("%s-%s", p.getLeft(), p.getRight()))
                          .toList())));
        }
      }
    }
    writer.flush();
  }

  public List<String> readFirstColumn(InputStream inputStream) {
    return readColumn(inputStream, 0);
  }

  public List<String> readColumn(InputStream inputStream, int position) {
    Set<String> records = new HashSet<>();

    try (Scanner scanner = new Scanner(inputStream, charset)) {
      while (scanner.hasNextLine()) {
        try (Scanner rowScanner = new Scanner(scanner.nextLine())) {
          rowScanner.useDelimiter(entriesDelimiter);
          int currentPos = 0;
          while (rowScanner.hasNext()) {
            if (currentPos > position) {
              break;
            } else if (currentPos == position) {
              String item = rowScanner.next();
              if (!CSVDataRecord.FIELDS.contains(item))
                Arrays.stream(item.split(entryPartsDelimiter)).findFirst().ifPresent(records::add);
            }
            currentPos++;
          }
        }
      }
    }
    return List.copyOf(records);
  }
}
