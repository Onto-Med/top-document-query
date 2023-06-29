package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.util.Values;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TextFunction {

  private String id;
  private String title;
  private NotationEnum notation;

  protected TextFunction(String id, String title, NotationEnum notation) {
    this.id = id;
    this.title = title;
    this.notation = notation;
  }

  public String getId() {
    return id;
  }

  public String toString(List<String> args) {
    if (notation == NotationEnum.PREFIX) return title + "(" + String.join(", ", args) + ")";
    else if (notation == NotationEnum.POSTFIX) return "(" + String.join(", ", args) + ")" + title;
    return "(" + String.join(" " + title + " ", args) + ")";
  }

  public String toStringValues(List<Value> args) {
    return toString(
        args.stream().map(Values::toStringWithoutDateTime).collect(Collectors.toList()));
  }

  public abstract Expression generate(List<Expression> args, SONG song);
}
