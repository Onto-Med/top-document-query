package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.List;

public class ElasticsearchNot extends Not {

  private static final ElasticsearchNot INSTANCE = new ElasticsearchNot();

  private ElasticsearchNot() {}

  public static ElasticsearchNot get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    args = song.generate(args);
    if (args.isEmpty()) return new Expression();
    return Exp.of("NOT " + song.getQuery(args.get(0))).type(SONG.EXPRESSION_TYPE_QUERY);
  }
}
