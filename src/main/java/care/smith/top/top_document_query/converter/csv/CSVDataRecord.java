package care.smith.top.top_document_query.converter.csv;

import care.smith.top.model.Value;

import java.util.List;

public class CSVDataRecord extends CSVRecord {
  private static final long serialVersionUID = 1L;

  public static List<String> FIELDS =
      List.of(
          "id",
          "title",
          "extract");

  public CSVDataRecord(String id, String title, String extract) {
    addEntry(id);
    addEntry(title);
    addEntry(extract);
  }
}
