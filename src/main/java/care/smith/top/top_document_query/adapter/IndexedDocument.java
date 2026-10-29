package care.smith.top.top_document_query.adapter;

import care.smith.top.model.Document;
import java.util.Objects;

/** Library-level provenance wrapper until the shared TOP API exposes index-aware documents. */
public record IndexedDocument(DocumentReference reference, Document document) {
  public IndexedDocument {
    Objects.requireNonNull(reference, "reference");
    Objects.requireNonNull(document, "document");
  }
}
