package care.smith.top.top_document_query.converter.csv;

import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.DocumentHit;
import care.smith.top.top_document_query.adapter.DocumentReference;
import java.util.regex.Pattern;
import care.smith.top.top_document_query.util.Entities;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;

public class DocumentCSV {

  private Charset charset = StandardCharsets.UTF_8;
  public static String entriesDelimiter = "\t";
  public static String entryPartsDelimiter = ";";
  private String language = null;

  @SuppressWarnings("unused")
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
    DocumentCSV.entriesDelimiter = entriesDelimiter;
    return this;
  }

  public DocumentCSV entryPartsDelimiter(String entryPartsDelimiter) {
    DocumentCSV.entryPartsDelimiter = entryPartsDelimiter;
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
      // DocumentID;FIELDNAME \t Score \t Title \t Begin1-End1;Begin2-End2
      if (highlights == null || highlights.isEmpty()) {
        writer.write(
            new CSVDataRecord(
                document.getDocumentId(),
                String.valueOf(document.getScore()),
                document.getDocument().getName(),
                nullValueString,
                Objects.requireNonNullElse(document.getIndex(), ""),
                Objects.requireNonNullElse(document.getDataSourceId(), "")));
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
                          .toList()),
                  Objects.requireNonNullElse(document.getIndex(), ""),
                  Objects.requireNonNullElse(document.getDataSourceId(), "")));
        }
      }
    }
    writer.flush();
  }

  /** Read exact identities from new exports; legacy ID-only exports cannot supply provenance. */
  public List<DocumentReference> readDocumentReferences(InputStream inputStream) {
    Set<DocumentReference> references = new LinkedHashSet<>();
    boolean headerFound = false;
    try (Scanner scanner = new Scanner(inputStream, charset)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("sep=")) continue;
        String[] columns = line.split(Pattern.quote(entriesDelimiter), -1);
        if (!headerFound) {
          if (!Arrays.asList(columns).equals(CSVDataRecord.FIELDS)) {
            throw new IllegalArgumentException("CSV does not contain index-aware document references");
          }
          headerFound = true;
          continue;
        }
        if (columns.length != CSVDataRecord.FIELDS.size()
            || columns[CSVDataRecord.Field.INDEX.columnIndex].isBlank()
            || columns[CSVDataRecord.Field.DATA_SOURCE.columnIndex].isBlank()) {
          throw new IllegalArgumentException("CSV row lacks document provenance");
        }
        references.add(new DocumentReference(
            columns[CSVDataRecord.Field.DATA_SOURCE.columnIndex],
            columns[CSVDataRecord.Field.INDEX.columnIndex],
            columns[CSVDataRecord.Field.ID.columnIndex].split(Pattern.quote(entryPartsDelimiter), 2)[0]));
      }
    }
    if (!headerFound) throw new IllegalArgumentException("CSV header is missing");
    return List.copyOf(references);
  }

  public List<String> readFirstColumn(InputStream inputStream) {
    return readColumn(inputStream, 0);
  }

  public List<String> readColumn(InputStream inputStream, int position) {
    if (position < 0) throw new IllegalArgumentException("Column position must not be negative");
    List<String> records = new ArrayList<>();
    boolean firstRecord = true;
    try (Scanner scanner = new Scanner(inputStream, charset)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("sep=")) continue;
        String[] columns = line.split(Pattern.quote(entriesDelimiter), -1);
        if (firstRecord) {
          firstRecord = false;
          List<String> header = Arrays.asList(columns);
          if (header.equals(CSVDataRecord.FIELDS)
              || header.equals(CSVDataRecord.FIELDS.subList(0, 4))) continue;
        }
        if (position >= columns.length) continue;
        String item = columns[position];
        records.add(position == CSVDataRecord.Field.ID.columnIndex
            ? item.split(Pattern.quote(entryPartsDelimiter), 2)[0] : item);
      }
    }
    return List.copyOf(records);
  }
}
