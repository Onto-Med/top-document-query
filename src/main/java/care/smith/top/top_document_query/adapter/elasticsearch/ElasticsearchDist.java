package care.smith.top.top_document_query.adapter.elasticsearch;

import care.smith.top.model.Expression;
import care.smith.top.model.Value;
import care.smith.top.top_document_query.SONG;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.util.Expressions;
import care.smith.top.top_document_query.util.Values;
import care.smith.top.top_document_query.util.builder.Exp;
import care.smith.top.top_document_query.util.builder.Val;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ElasticsearchDist extends Dist {

  private static final ElasticsearchDist INSTANCE = new ElasticsearchDist();

  private ElasticsearchDist() {}

  public static ElasticsearchDist get() {
    return INSTANCE;
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    if (args == null || args.size() != 2) return new Expression();
    Expression arg = song.generate(args.get(0));
    if (Expressions.isEmpty(arg)) return arg;
    AtomicInteger dist = new AtomicInteger(1);
    try {
      dist.set(Expressions.getNumberValue(args.get(1)).intValue());
    } catch (Exception e) {
      LOGGER.warning(String.format("Encountered error when getting NumberValue for '%s'. Using 1 as distance", args.get(1)));
    }
    List<Value> valsWithDist =
        arg.getValues().stream().map(v -> getDist(arg, v, dist.get())).collect(Collectors.toList());
    return Exp.of(valsWithDist).type(SONG.EXPRESSION_TYPE_TERMS_PROCESSED);
  }

  private Value getDist(Expression e, Value v, int d) {
    String t = Values.getStringValue(v);
    if (!StringUtils.containsWhitespace(t)) return v;
    if (Expressions.hasTermsInitial(e)) return Val.of("\"" + t + "\"~" + d);
    return Val.of(t + "~" + d);
  }
}
