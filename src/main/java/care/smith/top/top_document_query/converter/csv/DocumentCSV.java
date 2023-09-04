package care.smith.top.top_document_query.converter.csv;


import care.smith.top.model.Concept;
import care.smith.top.model.Entity;
import care.smith.top.top_document_query.adapter.AbstractDocument;
import care.smith.top.top_document_query.util.Entities;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DocumentCSV {

  private Charset charset = StandardCharsets.UTF_8;
  private String entriesDelimiter = ";";
  private String entryPartsDelimiter = ",";
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

  public void write(List<? extends AbstractDocument> documents, OutputStream outputStream) {
    CSVWriter writer = new CSVWriter(outputStream, entriesDelimiter, charset);
    writer.write(CSVDataRecord.FIELDS);
    for (AbstractDocument document : documents){
      writer.write(new CSVDataRecord(
          document.getId(),
          document.getName(),
          document.getText().substring(0, excerptLength))
      );
    }
    writer.flush();
  }
}
