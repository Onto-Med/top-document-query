package care.smith.top.top_document_query.util;

public class NLPUtils {
  public static String stringConformity(String s) {
    if (s == null || s.isEmpty()) return null;
    return s.toLowerCase().trim().replaceAll("\\s+", "_");
  }
}
