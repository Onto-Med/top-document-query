package care.smith.top.top_document_query.adapter;

/** An exact document identity; document IDs alone are not unique across indices. */
public record DocumentReference(String dataSourceId, String index, String documentId) {
  public DocumentReference {
    if (dataSourceId == null || dataSourceId.isBlank()
        || index == null || index.isBlank()
        || documentId == null || documentId.isBlank()) {
      throw new IllegalArgumentException("Document reference requires a data source, index, and ID");
    }
  }
}
