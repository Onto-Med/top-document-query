package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.top_document_query.adapter.IndexSelection;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IndexSelectionTest {
  @Test
  void selectionIsValidatedAndImmutableWithoutChangingConfiguration() {
    TextAdapterConfig config = new TextAdapterConfig();
    config.setIndex(new String[] {"one", "two"});
    List<String> request = new ArrayList<>(List.of("two"));
    IndexSelection selection = IndexSelection.select(config, request);
    request.clear();
    assertEquals(List.of("two"), selection.indices());
    assertEquals("two", selection.singleIndex());
    assertArrayEquals(new String[] {"one", "two"}, config.getIndex());
    assertThrows(UnsupportedOperationException.class, () -> selection.indices().add("one"));
    assertThrows(
        IllegalArgumentException.class, () -> IndexSelection.select(config, List.of("outside")));
    assertThrows(IllegalArgumentException.class, () -> IndexSelection.select(config, List.of()));
    assertThrows(
        IllegalArgumentException.class, () -> IndexSelection.select(config, List.of("one", "one")));
  }

  @Test
  void legacySelectionsUseTheSameNameNormalizationAsLegacyReads() {
    TextAdapterConfig config = new TextAdapterConfig();
    config.setIndex(new String[] {"Test Documents"});
    assertEquals(
        "test_documents", IndexSelection.select(config, List.of("Test Documents")).singleIndex());
    assertEquals(
        "test_documents", IndexSelection.select(config, List.of("test_documents")).singleIndex());
  }

  @Test
  void v2RequiresScopeForMultipleIndicesAndImportsRequireExactlyOne() {
    TextAdapterConfig config = new TextAdapterConfig();
    config.setIndex(new String[] {"one", "two"});
    assertEquals(List.of("one", "two"), IndexSelection.defaultRead(config).indices());
    assertThrows(
        IllegalStateException.class, () -> IndexSelection.configured(config).singleIndex());
    config.setConfigVersion(2);
    assertThrows(IllegalStateException.class, () -> IndexSelection.defaultRead(config));
    config.setIndex(new String[] {"one"});
    assertEquals("one", IndexSelection.defaultRead(config).singleIndex());
  }
}
