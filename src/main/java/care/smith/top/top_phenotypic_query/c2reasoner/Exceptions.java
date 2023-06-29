package care.smith.top.top_phenotypic_query.c2reasoner;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.ConstantEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.util.Expressions;
import java.util.List;
import java.util.Map;

public class Exceptions {

  public static void checkArgumentsHaveSameType(ExpressionFunction f, List<Expression> args) {
    String msg = "The arguments for the function '%s' have different data types!";
    DataType dt = null;
    for (Expression arg : args) {
      if (dt != null && dt != Expressions.getDataType(arg))
        throw new ArithmeticException(String.format(msg, f.getId()));
      else dt = Expressions.getDataType(arg);
    }
  }

  public static void checkArgumentsType(ExpressionFunction f, DataType dt, List<Expression> args) {
    for (Expression arg : args) checkArgumentType(f, dt, arg);
  }

  public static void checkArgumentType(ExpressionFunction f, DataType dt, Expression arg) {
    String msg = "The argument '%s' has a wrong data type for the function '%s'!";
    if (dt != Expressions.getDataType(arg))
      throw new ArithmeticException(String.format(msg, Expressions.toStringValues(arg), f.getId()));
  }

  public static void checkArgumentTypes(ExpressionFunction f, Expression arg, DataType... dts) {
    String msg = "The argument '%s' has a wrong data type for the function '%s'!";
    for (DataType dt : dts) if (dt == Expressions.getDataType(arg)) return;
    throw new ArithmeticException(String.format(msg, Expressions.toStringValues(arg), f.getId()));
  }

  //  public static void checkArgumentsContainLists(ExpressionFunction f, List<Expression> args) {
  //    String msg = "The arguments of the function '%s' must be single values!";
  //    for (Expression arg : args) {
  //      if (arg.getValues() != null && !arg.getValues().isEmpty())
  //        throw new ArithmeticException(String.format(msg, f.getId()));
  //    }
  //  }

  public static void checkArgumentHasValues(ExpressionFunction f, Expression arg) {
    String msg = "The argument of the function '%s' must have values!";
    if (arg.getValues() == null) throw new ArithmeticException(String.format(msg, f.getId()));
  }

  public static void checkArgumentHasValue(ExpressionFunction f, Expression arg) {
    String msg = "The argument of the function '%s' must have a single value!";
    if (!Expressions.hasSingleValue(arg))
      throw new ArithmeticException(String.format(msg, f.getId()));
  }

  public static void checkArgumentHasValueOfType(
      ExpressionFunction f, DataType dt, Expression arg) {
    checkArgumentHasValue(f, arg);
    checkArgumentType(f, dt, arg);
  }

  //  public static void checkArgumentIsNotNull(ExpressionFunction f, Expression arg) {
  //    String msg = "One argument of the function '%s' is null!";
  //    if (arg == null) throw new ArithmeticException(String.format(msg, f.getId()));
  //  }
  //
  //  public static void checkArgumentsAreNotNull(ExpressionFunction f, List<Expression> args) {
  //    String msg = "One argument of the function '%s' is null!";
  //    if (args == null) throw new ArithmeticException(String.format(msg, f.getId()));
  //  }

  public static void checkArgumentsNumber(ExpressionFunction f, List<Expression> args) {
    String msg = "The number of arguments (%s) is not suitable for the function '%s'!";
    if ((f.getMinArgumentNumber() != null && args.size() < f.getMinArgumentNumber())
        || (f.getMaxArgumentNumber() != null && args.size() > f.getMaxArgumentNumber()))
      throw new ArithmeticException(String.format(msg, args.size(), f.getId()));
  }

  public static void checkVariableIsSet(Expression exp, Map<String, Expression> vars) {
    String msg = "Variable '%s' is not set!";
    if (!vars.containsKey(exp.getEntityId()))
      throw new ArithmeticException(String.format(msg, exp.getEntityId()));
  }

  public static void checkConstantExists(String constantId, Map<String, ConstantEntity> constants) {
    String msg = "Constant '%s' does not exists!";
    if (!constants.containsKey(constantId))
      throw new ArithmeticException(String.format(msg, constantId));
  }

  public static void checkFunctionExists(Expression exp, Map<String, FunctionEntity> functions) {
    String msg = "Function '%s' does not exists!";
    if (!functions.containsKey(exp.getFunctionId()))
      throw new ArithmeticException(String.format(msg, exp.getFunctionId()));
  }
}
