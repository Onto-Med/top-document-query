package care.smith.top.top_phenotypic_query.converter.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

public class CSVWriter {

  private OutputStreamWriter writer;
  private String entriesDelimiter;

  public CSVWriter(OutputStream out, String entriesDelimiter, Charset charset) {
    if ("|".equals(entriesDelimiter.trim()))
      throw new IllegalArgumentException("The pipe symbol '|' must not be used as delimiter!");
    this.entriesDelimiter = entriesDelimiter;
    this.writer = new OutputStreamWriter(out, charset);
  }

  public void write(List<String> record) {
    try {
      writer.write(String.join(entriesDelimiter, record) + System.lineSeparator());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void flush() {
    try {
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
