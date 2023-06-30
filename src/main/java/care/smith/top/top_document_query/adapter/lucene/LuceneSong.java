package care.smith.top.top_document_query.adapter.lucene;

import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.util.Entities;

public class LuceneSong extends SONG {

  private static LuceneSong INSTANCE = new LuceneSong();

  public static LuceneSong get() {
    return INSTANCE;
  }

  private LuceneSong() {
    super(LuceneAnd.get(), LuceneOr.get(), LuceneNot.get(), LuceneDist.get());
  }

  public LuceneSong lang(String lang) {
    setLang(lang);
    return this;
  }

  public LuceneSong concepts(Entities concepts) {
    setConcepts(concepts);
    return this;
  }
}
