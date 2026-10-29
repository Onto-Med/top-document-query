package care.smith.top.top_document_query.converter.csv;

import java.util.List;

public class CSVDataRecord extends CSVRecord {
  private static final long serialVersionUID = 1L;

  public enum Field {
    ID(0, "id"),
    SCORE(1, "score"),
    TITLE(2, "title"),
    OFFSETS(3, "offsets"),
    INDEX(4, "index"),
    DATA_SOURCE(5, "dataSource");

    public final int columnIndex;
    public final String columnName;

    Field(int columnIndex, String columnName) {
      this.columnIndex = columnIndex;
      this.columnName = columnName;
    }
  }

  public static List<String> FIELDS =
      List.of(
          Field.ID.columnName,
          Field.SCORE.columnName,
          Field.TITLE.columnName,
          Field.OFFSETS.columnName,
          Field.INDEX.columnName,
          Field.DATA_SOURCE.columnName);

  public CSVDataRecord(String id, String score, String title, String offsets) {
    this(id, score, title, offsets, "", "");
  }

  public CSVDataRecord(
      String id, String score, String title, String offsets, String index, String dataSource) {
    addEntry(id);
    addEntry(score);
    addEntry(title);
    addEntry(offsets);
    addEntry(index);
    addEntry(dataSource);
  }
}
