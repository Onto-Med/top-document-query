package care.smith.top.top_phenotypic_query.song.adapter.lucene;

import care.smith.top.model.Expression;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.song.functions.And;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;
import java.util.stream.Collectors;

public class LuceneAnd extends And {

  private static LuceneAnd INSTANCE = new LuceneAnd();

  private LuceneAnd() {}

  public static LuceneAnd get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    args = song.generate(args);
    if (args.isEmpty()) return new Expression();
    String query = args.stream().map(a -> song.getQuery(a)).collect(Collectors.joining(" AND "));
    return Exp.of("(" + query + ")").type(SONG.EXPRESSION_TYPE_QUERY);
  }
}
