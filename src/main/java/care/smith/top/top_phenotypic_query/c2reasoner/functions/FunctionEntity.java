package care.smith.top.top_phenotypic_query.c2reasoner.functions;

import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.C2R;
import care.smith.top.top_phenotypic_query.util.Values;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FunctionEntity {
  private static final String DEFAULT_TYPE = "none";

  private final ExpressionFunction function;

  protected FunctionEntity(String title, NotationEnum notation) {
    this.function =
        new ExpressionFunction()
            .id(getClass().getSimpleName())
            .title(title)
            .notation(notation)
            .type(getType());
  }

  protected FunctionEntity(
      String title, NotationEnum notation, int minArgumentNumber, int maxArgumentNumber) {
    this(title, notation);
    minArgumentNumber(minArgumentNumber);
    maxArgumentNumber(maxArgumentNumber);
  }

  public abstract Expression calculate(List<Expression> args, C2R c2r);

  public ExpressionFunction getFunction() {
    return function;
  }

  public String getFunctionId() {
    return function.getId();
  }

  public String getType() {
    String packageName = this.getClass().getPackageName();
    String type = packageName.substring(packageName.lastIndexOf('.') + 1);
    return "".equals(type) ? DEFAULT_TYPE : type;
  }

  public String toString(List<String> args) {
    if (function.getNotation() == NotationEnum.PREFIX)
      return function.getTitle() + "(" + String.join(", ", args) + ")";
    else if (function.getNotation() == NotationEnum.POSTFIX)
      return "(" + String.join(", ", args) + ")" + function.getTitle();
    return "(" + String.join(" " + function.getTitle() + " ", args) + ")";
  }

  public String toStringValues(List<Value> args) {
    return toString(
        args.stream().map(Values::toStringWithoutDateTime).collect(Collectors.toList()));
  }

  protected void maxArgumentNumber(int num) {
    function.maxArgumentNumber(num);
  }

  protected void minArgumentNumber(int num) {
    function.minArgumentNumber(num);
  }
}
