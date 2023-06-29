package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.DataType;
import care.smith.top.model.Expression;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.In;
import care.smith.top.top_phenotypic_query.song.SONG;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Expressions {

  public static boolean hasValues(Expression exp) {
    return exp.getValues() != null && !exp.getValues().isEmpty();
  }

  public static boolean hasSingleValue(Expression exp) {
    return exp.getValues() != null && exp.getValues().size() == 1;
  }

  public static Value getValue(Expression exp) {
    if (hasValues(exp)) return exp.getValues().get(0);
    return null;
  }

  public static BigDecimal getNumberValue(Expression exp) {
    return Values.getNumberValue(getValue(exp));
  }

  public static String getStringValue(Expression exp) {
    return Values.getStringValue(getValue(exp));
  }

  public static boolean hasBlankStringValue(Expression exp) {
    String val = Values.getStringValue(getValue(exp));
    return val != null && val.isBlank();
  }

  public static Boolean getBooleanValue(Expression exp) {
    return Values.getBooleanValue(getValue(exp));
  }

  public static LocalDateTime getDateTimeValue(Expression exp) {
    return Values.getDateTimeValue(getValue(exp));
  }

  public static List<BigDecimal> getNumberValues(Expression exp) {
    return Values.getNumberValues(exp.getValues());
  }

  public static List<String> getStringValues(Expression exp) {
    return Values.getStringValues(exp.getValues());
  }

  public static List<Boolean> getBooleanValues(Expression exp) {
    return Values.getBooleanValues(exp.getValues());
  }

  public static List<LocalDateTime> getDateTimeValues(Expression exp) {
    return Values.getDateTimeValues(exp.getValues());
  }

  public static boolean hasValueTrue(Expression exp) {
    return Values.getBooleanValue(getValue(exp));
  }

  public static boolean hasValueFalse(Expression exp) {
    return !hasValueTrue(exp);
  }

  public static DataType getDataType(Expression exp) {
    if (hasValues(exp)) return getValue(exp).getDataType();
    if (exp.getRestriction() != null) return exp.getRestriction().getType();
    return null;
  }

  public static boolean hasStringType(Expression exp) {
    return getDataType(exp) == DataType.STRING;
  }

  public static boolean hasNumberType(Expression exp) {
    return getDataType(exp) == DataType.NUMBER;
  }

  public static boolean hasBooleanType(Expression exp) {
    return getDataType(exp) == DataType.BOOLEAN;
  }

  public static boolean hasDateTimeType(Expression exp) {
    return getDataType(exp) == DataType.DATE_TIME;
  }

  public static String toStringValues(Expression exp) {
    if (exp.getValues() != null) return Values.toString(exp.getValues());
    return null;
  }

  public static Set<String> getDirectVariables(Expression exp, Entities phenotypes) {
    return getVariables(exp, phenotypes, true);
  }

  public static Set<String> getVariables(Expression exp, Entities phenotypes) {
    return getVariables(exp, phenotypes, false);
  }

  public static Set<String> getVariables(Expression exp, Entities phenotypes, boolean direct) {
    Set<String> vars = new LinkedHashSet<>();
    addVariables(exp, vars, phenotypes, direct);
    return vars;
  }

  private static void addVariables(
      Expression exp, Set<String> vars, Entities phenotypes, boolean direct) {
    if (exp == null) return;
    if (exp.getEntityId() != null) {
      vars.add(exp.getEntityId());
      Phenotype varPhe = phenotypes.getPhenotype(exp.getEntityId());
      if (varPhe != null && !direct) {
        Expression varPheExp = varPhe.getExpression();
        if (varPheExp != null) addVariables(varPheExp, vars, phenotypes, direct);
      }
    } else if (exp.getArguments() != null && !isSingleRestriction(exp, phenotypes))
      for (Expression arg : exp.getArguments()) addVariables(arg, vars, phenotypes, direct);
  }

  private static boolean isSingleRestriction(Expression e, Entities phenotypes) {
    return In.get().getFunctionId().equals(e.getFunctionId())
        && Phenotypes.isSinglePhenotype(
            phenotypes.getPhenotype(e.getArguments().get(0).getEntityId()));
  }

  public static boolean containsNegation(Expression exp, Entities phenotypes) {
    if (exp.getFunctionId() != null) {
      if (Not.get().getFunctionId().equals(exp.getFunctionId())) return true;
      for (Expression arg : exp.getArguments()) if (containsNegation(arg, phenotypes)) return true;
    } else if (exp.getEntityId() != null) {
      Phenotype phe = phenotypes.getPhenotype(exp.getEntityId());
      if (phe.getExpression() == null) return false;
      return containsNegation(phe.getExpression(), phenotypes);
    }
    return false;
  }

  public static boolean isEmpty(Expression exp) {
    return exp.getConstantId() == null
        && exp.getEntityId() == null
        && exp.getFunctionId() == null
        && exp.getRestriction() == null
        && !hasValues(exp);
  }

  public static boolean hasTermsInitial(Expression exp) {
    return SONG.EXPRESSION_TYPE_TERMS_INITIAL.equals(exp.getType());
  }

  public static boolean hasTermsProcessed(Expression exp) {
    return SONG.EXPRESSION_TYPE_TERMS_PROCESSED.equals(exp.getType());
  }

  public static boolean hasQuery(Expression exp) {
    return SONG.EXPRESSION_TYPE_QUERY.equals(exp.getType());
  }
}
