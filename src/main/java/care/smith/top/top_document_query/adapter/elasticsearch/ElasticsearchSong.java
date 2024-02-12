package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.util.Entities;

public class ElasticsearchSong extends SONG {

  private static final ElasticsearchSong INSTANCE = new ElasticsearchSong();

  public static ElasticsearchSong get() {
    return INSTANCE;
  }

  private ElasticsearchSong() {
    super(
        ElasticsearchAnd.get(),
        ElasticsearchOr.get(),
        ElasticsearchNot.get(),
        ElasticsearchDist.get());
  }

  public ElasticsearchSong lang(String lang) {
    setLang(lang);
    return this;
  }

  public ElasticsearchSong concepts(Entities concepts) {
    setConcepts(concepts);
    return this;
  }
}
