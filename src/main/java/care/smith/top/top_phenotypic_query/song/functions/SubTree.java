package care.smith.top.top_phenotypic_query.song.functions;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.ExpressionFunction.NotationEnum;
import care.smith.top.top_phenotypic_query.song.SONG;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.util.List;

public class SubTree extends TextFunction {

  public static final String ID = "SubTree";
  private static final NotationEnum NOTATION = NotationEnum.PREFIX;

  public static final ExpressionFunction FUNCTION =
      new ExpressionFunction()
          .id(ID)
          .title(ID)
          .notation(NOTATION)
          .minArgumentNumber(1)
          .maxArgumentNumber(1);

  private static SubTree INSTANCE = new SubTree();

  private SubTree() {
    super(ID, ID, NOTATION);
  }

  public static SubTree get() {
    return INSTANCE;
  }

  public static Expression of(Expression arg) {
    return Exp.function(ID, arg);
  }

  public static Expression of(Entity arg) {
    return of(Exp.of(arg));
  }

  public static Expression of(String phenotypeId) {
    return of(Exp.ofEntity(phenotypeId));
  }

  @Override
  public Expression generate(List<Expression> args, SONG song) {
    if (args.isEmpty()) return new Expression();
    Expression arg = args.get(0);
    if (arg.getEntityId() == null) return new Expression();
    return song.getTermsExpression(arg.getEntityId(), SONG.EXPRESSION_TYPE_TERMS_INITIAL, true);
  }
}
