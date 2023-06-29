package care.smith.top.top_phenotypic_query.converter.csv;

import java.util.List;

public class CSVRecordCompositeEntries extends CSVRecord {

  private static final long serialVersionUID = 1L;
  private String entryPartsDelimiter;

  public CSVRecordCompositeEntries(String entryPartsDelimiter) {
    if ("|".equals(entryPartsDelimiter.trim()))
      throw new IllegalArgumentException("The pipe symbol '|' must not be used as delimiter!");
    this.entryPartsDelimiter = entryPartsDelimiter;
  }

  public void addEntry(List<String> entry) {
    if (entry == null || entry.isEmpty()) add("");
    else add(String.join(entryPartsDelimiter, entry));
  }
}
