package care.smith.top.top_phenotypic_query.song.adapter.lucene;

import care.smith.top.model.Expression;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.song.functions.Not;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class LuceneNot extends Not {

  private static LuceneNot INSTANCE = new LuceneNot();

  private LuceneNot() {}

  public static LuceneNot get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    args = song.generate(args);
    if (args.isEmpty()) return new Expression();
    return Exp.of("NOT " + song.getQuery(args.get(0))).type(SONG.EXPRESSION_TYPE_QUERY);
  }
}
