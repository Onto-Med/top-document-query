package care.smith.top.top_document_query.converter.csv;

import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.util.Entities;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DocumentCSV {

  private Charset charset = StandardCharsets.UTF_8;
  private String entriesDelimiter = "‖";
  private String entryPartsDelimiter = ";";
  private String language = null;
  private int excerptLength = 100;

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
    writer.write(CSVDataRecord.FIELDS); //ToDo: need to change field names
    for (DocumentHit document : documents) {
      Map<String, List<Pair<Integer, Integer>>> highlights = document.getHighlights();
      //ToDo: fix with new highlights!
      // DocumentID;FIELDNAME ‖ Score ‖ Title ‖ Begin1-End1;Begin2-End2
      String excerpt;
      if (highlights == null || highlights.isEmpty()) {
        excerpt = document.getDocument().getText().substring(0, excerptLength).replace("\n", " ");
      } else {
        StringBuilder sb = new StringBuilder();
        for (List<Pair<Integer, Integer>> hl : highlights.values()) {
          sb.append(String.join(String.format(" %s ", entryPartsDelimiter), hl).replace("\n", " "));
          sb.append(String.format(" %s ", entryPartsDelimiter));
        }
        excerpt = sb.substring(0, sb.length() - 2);
      }
      writer.write(
          new CSVDataRecord(
              document.getDocumentId(),
              String.valueOf(document.getScore()),
              document.getDocument().getName(),
              // ToDo: encoding is wrong
              // ToDo: more meaningful excerpt (right now, only the first excerptLength characters
              // are used)
              excerpt));
    }
    writer.flush();
  }

  public List<String> readFirstColumn(InputStream inputStream) {
    return readColumn(inputStream, 0);
  }

  public List<String> readColumn(InputStream inputStream, int position) {
    List<String> records = new ArrayList<>();
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
              if (!CSVDataRecord.FIELDS.contains(item)) records.add(item);
            }
            currentPos++;
          }
        }
      }
    }
    return records;
  }
}
