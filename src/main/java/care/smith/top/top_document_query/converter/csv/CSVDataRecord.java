package care.smith.top.top_document_query.converter.csv;

import java.util.List;

public class CSVDataRecord extends CSVRecord {
  private static final long serialVersionUID = 1L;

  public static List<String> FIELDS = List.of("id", "score", "title", "extract");

  public CSVDataRecord(String id, String score, String title, String extract) {
    addEntry(id);
    addEntry(score);
    addEntry(title);
    addEntry(extract);
  }
}
