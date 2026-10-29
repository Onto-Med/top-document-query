package care.smith.top.top_document_query.adapter;

import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.util.NLPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Immutable operation scope; never changes the adapter's configured allowlist. */
public record IndexSelection(List<String> indices) {
  public IndexSelection {
    if (indices == null || indices.isEmpty()
        || indices.stream().anyMatch(index -> index == null || index.isBlank())) {
      throw new IllegalArgumentException("Select at least one nonblank index");
    }
    if (indices.stream().distinct().count() != indices.size()) {
      throw new IllegalArgumentException("Selected indices must be unique");
    }
    indices = List.copyOf(indices);
  }

  public static IndexSelection select(TextAdapterConfig config, Collection<String> indices) {
    IndexSelection selection = new IndexSelection(indices == null ? null : new ArrayList<>(indices));
    if (config.getConfigVersion() == 1) {
      selection = new IndexSelection(selection.indices().stream().map(NLPUtils::stringConformity).toList());
    }
    List<String> allowed = configured(config).indices();
    if (!allowed.containsAll(selection.indices())) {
      throw new IllegalArgumentException("Selected indices must belong to the adapter's configured indices");
    }
    return selection;
  }

  public static IndexSelection configured(TextAdapterConfig config) {
    return new IndexSelection(
        Arrays.stream(config.getIndex())
            .map(index -> config.getConfigVersion() == 1 ? NLPUtils.stringConformity(index) : index)
            .toList());
  }

  public static IndexSelection defaultRead(TextAdapterConfig config) {
    IndexSelection selection = configured(config);
    if (config.getConfigVersion() == 2 && selection.indices().size() != 1) {
      throw new IllegalStateException("Select indices explicitly for a multi-index v2 adapter");
    }
    return selection;
  }

  public String singleIndex() {
    if (indices.size() != 1) {
      throw new IllegalStateException("This operation requires exactly one selected index");
    }
    return indices.getFirst();
  }
}
