package care.smith.top.top_phenotypic_query.data_adapter.config;

public class CSVSettings {

  private String charset;
  private String entriesDelimiter;
  private String entryPartsDelimiter;

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public String getEntriesDelimiter() {
    return entriesDelimiter;
  }

  public void setEntriesDelimiter(String entriesDelimiter) {
    this.entriesDelimiter = entriesDelimiter;
  }

  public String getEntryPartsDelimiter() {
    return entryPartsDelimiter;
  }

  public void setEntryPartsDelimiter(String entryPartsDelimiter) {
    this.entryPartsDelimiter = entryPartsDelimiter;
  }

  @Override
  public String toString() {
    return "CSVSettings [charset="
        + charset
        + ", entriesDelimiter="
        + entriesDelimiter
        + ", entryPartsDelimiter="
        + entryPartsDelimiter
        + "]";
  }
}
