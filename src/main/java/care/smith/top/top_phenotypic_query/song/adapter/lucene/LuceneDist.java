package care.smith.top.top_phenotypic_query.song.adapter.lucene;

import care.smith.top.model.Expression;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.song.functions.Dist;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class LuceneDist extends Dist {

  private static LuceneDist INSTANCE = new LuceneDist();

  private LuceneDist() {}

  public static LuceneDist get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    if (args == null || args.size() != 2) return new Expression();
    Expression arg = song.generate(args.get(0));
    if (Expressions.isEmpty(arg)) return arg;
    int dist = Expressions.getNumberValue(args.get(1)).intValue();
    List<Value> valsWithDist =
        arg.getValues().stream().map(v -> getDist(arg, v, dist)).collect(Collectors.toList());
    return Exp.of(valsWithDist).type(SONG.EXPRESSION_TYPE_TERMS_PROCESSED);
  }

  private Value getDist(Expression e, Value v, int d) {
    String t = Values.getStringValue(v);
    if (!StringUtils.containsWhitespace(t)) return v;
    if (Expressions.hasTermsInitial(e)) return Val.of("\"" + t + "\"~" + d);
    return Val.of(t + "~" + d);
  }
}
