package care.smith.top.top_document_query.tests;

import care.smith.top.top_document_query.adapter.DocumentHit;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class DocumentHitTest {

  @Test
  void setHighlights() {
      Map<String, List<Pair<Integer, Integer>>> offsets = Map.of(
              "text", List.of(
                      Pair.of(133, 140),
                      Pair.of(161, 167),
                      Pair.of(229, 237),
                      Pair.of(242, 248),
                      Pair.of(291, 305)
              )
      );
    Map<String, List<String>> highlights =
        Map.of(
            "text",
            List.of(
                "Opiate: (Heroin, Morphium, Metadon, Opium, Subutex, Tilidin, Tramadol, Codein) sei seit Dezember 2016 clean, vorher regelmäßig 32 mg <em>Subutex</em> nasal, immer wieder <em>Heroin</em> und gelegentlich Kokain. Morphium, Methadon, Opium, Tilidin, <em>Tramadol</em> und <em>Codein</em> war nur eine Nebensache). Amphetamine und <em>Benzodiazepine</em> nur Selten."));
    DocumentHit hit = new DocumentHit("documentId", null, highlights, 0.0);
    assertEquals(offsets, hit.getHighlights());
  }
}
