package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.BooleanValue;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeValue;
import care.smith.top.model.Expression;
import care.smith.top.model.NumberValue;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Values {

  public static Comparator<Expression> EXP_DATE_COMPARATOR =
      new Comparator<Expression>() {
        @Override
        public int compare(Expression e1, Expression e2) {
          if (!Expressions.hasValues(e1) || !Expressions.hasValues(e2)) return 0;
          LocalDateTime d1 = getDateTime(Expressions.getValue(e1));
          LocalDateTime d2 = getDateTime(Expressions.getValue(e2));
          if (d1 == null || d2 == null) return 0;
          return d1.compareTo(d2);
        }
      };

  public static Comparator<Value> VALUE_DATE_COMPARATOR =
      new Comparator<Value>() {
        @Override
        public int compare(Value v1, Value v2) {
          LocalDateTime d1 = getDateTime(v1);
          LocalDateTime d2 = getDateTime(v2);
          if (d1 == null || d2 == null) return 0;
          return d1.compareTo(d2);
        }
      };

  public static Comparator<Expression> VALUE_COMPARATOR =
      new Comparator<Expression>() {
        @Override
        public int compare(Expression e1, Expression e2) {
          if (!Expressions.hasValues(e1) || !Expressions.hasValues(e2)) return 0;
          BigDecimal v1 = Expressions.getNumberValue(e1);
          BigDecimal v2 = Expressions.getNumberValue(e2);
          if (v1 == null || v2 == null) return 0;
          return v1.compareTo(v2);
        }
      };

  public static BigDecimal toDecimal(Number num) {
    if (num == null) return null;
    return new BigDecimal(num.toString());
  }

  public static List<BigDecimal> toDecimals(Number... nums) {
    return Stream.of(nums).map(n -> toDecimal(n)).collect(Collectors.toList());
  }

  public static String toString(Value val) {
    return addDateTime(toStringWithoutDateTime(val), val.getDateTime());
  }

  private static String addDateTime(String str, LocalDateTime dateTime) {
    return (dateTime == null) ? str : str + "|" + DateUtil.format(dateTime);
  }

  public static String toString(List<Value> vals) {
    if (vals == null) return null;
    return vals.stream().map(v -> toString(v)).collect(Collectors.toList()).toString();
  }

  public static String toStringWithoutDateTime(Value val) {
    if (val instanceof NumberValue) return toStringWithoutDateTime((NumberValue) val);
    if (val instanceof DateTimeValue) return toStringWithoutDateTime((DateTimeValue) val);
    if (val instanceof BooleanValue) return toStringWithoutDateTime((BooleanValue) val);
    return toStringWithoutDateTime((StringValue) val);
  }

  public static String toStringWithoutDateTime(StringValue val) {
    return val.getValue();
  }

  public static String toStringWithoutDateTime(BooleanValue val) {
    return val.isValue().toString();
  }

  public static String toStringWithoutDateTime(NumberValue val) {
    return val.getValue().toPlainString();
  }

  public static String toStringWithoutDateTime(DateTimeValue val) {
    return DateUtil.format(val.getValue());
  }

  public static String getStringValue(Value val) {
    if (val == null) return null;
    return ((StringValue) val).getValue();
  }

  public static BigDecimal getNumberValue(Value val) {
    if (val == null) return null;
    return ((NumberValue) val).getValue();
  }

  public static LocalDateTime getDateTimeValue(Value val) {
    if (val == null) return null;
    return ((DateTimeValue) val).getValue();
  }

  public static Boolean getBooleanValue(Value val) {
    if (val == null) return null;
    return ((BooleanValue) val).isValue();
  }

  public static List<String> getStringValues(List<Value> vals) {
    return vals.stream().map(v -> getStringValue(v)).collect(Collectors.toList());
  }

  public static List<BigDecimal> getNumberValues(List<Value> vals) {
    return vals.stream().map(v -> getNumberValue(v)).collect(Collectors.toList());
  }

  public static List<LocalDateTime> getDateTimeValues(List<Value> vals) {
    return vals.stream().map(v -> getDateTimeValue(v)).collect(Collectors.toList());
  }

  public static List<Boolean> getBooleanValues(List<Value> vals) {
    return vals.stream().map(v -> getBooleanValue(v)).collect(Collectors.toList());
  }

  public static boolean contains(List<Value> set, Value value) {
    for (Value member : set) {
      if (hasStringType(value)) {
        if (getStringValue(value).equals(getStringValue(member))) return true;
      } else if (hasNumberType(value)) {
        if (getNumberValue(value).compareTo(getNumberValue(member)) == 0) return true;
      } else if (hasDateTimeType(value)) {
        if (getDateTimeValue(value).equals(getDateTimeValue(member))) return true;
      } else if (hasBooleanType(value)) {
        if (getBooleanValue(value).equals(getBooleanValue(member))) return true;
      }
    }
    return false;
  }

  public static boolean contains(Map<RestrictionOperator, Value> interval, Value value) {
    for (RestrictionOperator oper : interval.keySet()) {
      if (!checkLimit(value, oper, interval.get(oper))) return false;
    }
    return true;
  }

  private static boolean checkLimit(Value val, RestrictionOperator oper, Value lim) {
    if (oper == RestrictionOperator.GREATER_THAN && compare(val, lim) > 0) return true;
    if (oper == RestrictionOperator.GREATER_THAN_OR_EQUAL_TO && compare(val, lim) >= 0) return true;
    if (oper == RestrictionOperator.LESS_THAN && compare(val, lim) < 0) return true;
    if (oper == RestrictionOperator.LESS_THAN_OR_EQUAL_TO && compare(val, lim) <= 0) return true;
    return false;
  }

  public static int compare(Value v1, Value v2) {
    if (hasNumberType(v1) && hasNumberType(v2))
      return getNumberValue(v1).compareTo(getNumberValue(v2));
    if (hasDateTimeType(v1) && hasDateTimeType(v2))
      return getDateTimeValue(v1).compareTo(getDateTimeValue(v2));
    if (hasStringType(v1) && hasStringType(v2))
      return getStringValue(v1).compareTo(getStringValue(v2));
    if (hasBooleanType(v1) && hasBooleanType(v2))
      return getBooleanValue(v1).compareTo(getBooleanValue(v2));
    return 0;
  }

  public static boolean hasStringType(Value v) {
    return v.getDataType() == DataType.STRING;
  }

  public static boolean hasNumberType(Value v) {
    return v.getDataType() == DataType.NUMBER;
  }

  public static boolean hasBooleanType(Value v) {
    return v.getDataType() == DataType.BOOLEAN;
  }

  public static boolean hasDateTimeType(Value v) {
    return v.getDataType() == DataType.DATE_TIME;
  }

  public static LocalDateTime getStartDateTime(Value v) {
    if (v.getStartDateTime() != null) return v.getStartDateTime();
    return v.getDateTime();
  }

  public static LocalDateTime getEndDateTime(Value v) {
    if (v.getEndDateTime() != null) return v.getEndDateTime();
    return v.getDateTime();
  }

  public static LocalDateTime getDateTime(Value v) {
    if (v.getDateTime() != null) return v.getDateTime();
    return v.getStartDateTime();
  }

  public static boolean overlaps1(Value v1, Value v2, int maxDistance) {
    return overlaps1(getStartDateTime(v1), getEndDateTime(v1), getStartDateTime(v2), maxDistance);
  }

  public static boolean overlaps2(Value v1, Value v2, int maxDistance1, int maxDistance2) {
    return overlaps1(v1, v2, maxDistance1) || overlaps1(v2, v1, maxDistance2);
  }

  private static boolean overlaps1(
      LocalDateTime v1Start, LocalDateTime v1End, LocalDateTime v2Start, int maxDistance) {
    return !v1Start.isAfter(v2Start) && !v1End.plusHours(maxDistance).isBefore(v2Start);
  }

  public static boolean startsBefore(Value v1, Value v2) {
    return getStartDateTime(v1).isBefore(getStartDateTime(v2));
  }

  public static boolean endsBefore(Value v1, Value v2) {
    return getEndDateTime(v1).isBefore(getEndDateTime(v2));
  }
}
