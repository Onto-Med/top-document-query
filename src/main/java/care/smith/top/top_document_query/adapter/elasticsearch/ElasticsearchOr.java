package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.util.builder.Exp;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticsearchOr extends Or {

  private static final ElasticsearchOr INSTANCE = new ElasticsearchOr();

  private ElasticsearchOr() {}

  public static ElasticsearchOr get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    args = song.generate(args);
    if (args.isEmpty()) return new Expression();
    String query = args.stream().map(song::getQuery).collect(Collectors.joining(" OR "));
    return Exp.of("(" + query + ")").type(SONG.EXPRESSION_TYPE_QUERY);
  }
}
