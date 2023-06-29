package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.BooleanRestriction;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.NumberRestriction;
import care.smith.top.model.Quantifier;
import care.smith.top.model.Restriction;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringRestriction;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.data_adapter.DataAdapterSettings;
import care.smith.top.top_phenotypic_query.ucum.UCUM;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;

public class Restrictions {

  public static boolean hasNumberType(Restriction r) {
    if (r.getType() != null) return r.getType() == DataType.NUMBER;
    return r instanceof NumberRestriction;
  }

  public static boolean hasStringType(Restriction r) {
    if (r.getType() != null) return r.getType() == DataType.STRING;
    return r instanceof StringRestriction;
  }

  public static boolean hasBooleanType(Restriction r) {
    if (r.getType() != null) return r.getType() == DataType.BOOLEAN;
    return r instanceof BooleanRestriction;
  }

  public static boolean hasDateTimeType(Restriction r) {
    if (r.getType() != null) return r.getType() == DataType.DATE_TIME;
    return r instanceof DateTimeRestriction;
  }

  public static boolean hasConceptType(Restriction r) {
    if (!hasStringType(r)) return false;
    List<String> vals = ((StringRestriction) r).getValues();
    return vals != null && !vals.isEmpty() && vals.get(0).startsWith("http");
  }

  public static String toString(Restriction r) {
    if (r == null) return "|generally valid|";
    if (hasDateTimeType(r) && isEntirePeriod((DateTimeRestriction) r)) return "|entire period|";
    String s = "";
    if (r.getQuantifier() != null) s += "|" + r.getQuantifier().name();
    if (r.getCardinality() != null) s += "|" + r.getCardinality().intValue();
    if (hasInterval(r)) s += getIntervalAsString(r);
    else if (hasValues(r)) s += "|" + getValuesAsString(r);
    return s + "|";
  }

  private static Restriction copy(Restriction r, boolean withBasicAttributes) {
    Restriction copy = null;
    if (hasNumberType(r)) {
      NumberRestriction nr = (NumberRestriction) r;
      copy =
          new NumberRestriction()
              .maxOperator(nr.getMaxOperator())
              .minOperator(nr.getMinOperator())
              .values(nr.getValues());
    } else if (hasDateTimeType(r)) {
      DateTimeRestriction dr = (DateTimeRestriction) r;
      copy =
          new DateTimeRestriction()
              .maxOperator(dr.getMaxOperator())
              .minOperator(dr.getMinOperator())
              .values(dr.getValues());
    } else if (hasBooleanType(r)) {
      BooleanRestriction br = (BooleanRestriction) r;
      copy = new BooleanRestriction().values(br.getValues());
    } else {
      StringRestriction sr = (StringRestriction) r;
      copy = new StringRestriction().values(sr.getValues());
    }

    if (withBasicAttributes)
      copy.cardinality(r.getCardinality()).quantifier(r.getQuantifier()).type(r.getType());

    return copy;
  }

  public static Restriction copy(Restriction r) {
    return copy(r, true);
  }

  public static Restriction copyWithoutBasicAttributes(Restriction r) {
    return copy(r, false);
  }

  public static Restriction setType(Restriction r) {
    if (hasNumberType(r)) return r.type(DataType.NUMBER);
    if (hasDateTimeType(r)) return r.type(DataType.DATE_TIME);
    if (hasBooleanType(r)) return r.type(DataType.BOOLEAN);
    return r.type(DataType.STRING);
  }

  public static boolean hasInterval(Restriction r) {
    if (hasNumberType(r)) {
      NumberRestriction nr = (NumberRestriction) r;
      return nr.getMinOperator() != null || nr.getMaxOperator() != null;
    }
    if (hasDateTimeType(r)) {
      DateTimeRestriction dr = (DateTimeRestriction) r;
      return dr.getMinOperator() != null || dr.getMaxOperator() != null;
    }
    return false;
  }

  public static Map<String, String> getIntervalAsStringMap(
      Restriction r, DataAdapterSettings format) {
    Map<String, String> limits = new LinkedHashMap<>();
    if (hasNumberType(r)) {
      NumberRestriction nr = (NumberRestriction) r;
      if (nr.getMinOperator() != null)
        limits.put(format(nr.getMinOperator(), format), format(nr.getValues().get(0), format));
      if (nr.getMaxOperator() != null)
        limits.put(format(nr.getMaxOperator(), format), format(nr.getValues().get(1), format));
    }
    if (hasDateTimeType(r)) {
      DateTimeRestriction dr = (DateTimeRestriction) r;
      if (dr.getMinOperator() != null)
        limits.put(format(dr.getMinOperator(), format), format(dr.getValues().get(0), format));
      if (dr.getMaxOperator() != null)
        limits.put(format(dr.getMaxOperator(), format), format(dr.getValues().get(1), format));
    }
    return limits;
  }

  public static Map<String, String> getIntervalAsStringMap(Restriction r) {
    return getIntervalAsStringMap(r, null);
  }

  public static String getIntervalAsString(Restriction r) {
    String s = "";
    Map<String, String> in = getIntervalAsStringMap(r);
    for (String op : in.keySet()) s += "|" + op + " " + in.get(op);
    return s;
  }

  public static boolean hasValues(Restriction r) {
    if (hasNumberType(r)) return !getNumberValues(r).isEmpty();
    if (hasDateTimeType(r)) return !getDateTimeValues(r).isEmpty();
    if (hasBooleanType(r)) return !getBooleanValues(r).isEmpty();
    return !getStringValues(r).isEmpty();
  }

  public static String getValuesAsString(Restriction r) {
    return getValuesAsString(r, null);
  }

  public static String getValuesAsString(Restriction r, DataAdapterSettings format) {
    if (hasNumberType(r))
      return format(
          getNumberValues(r).stream().map(v -> format(v, format)).map(String.class::cast), format);
    if (hasDateTimeType(r))
      return format(
          getDateTimeValues(r).stream().map(v -> format(v, format)).map(String.class::cast),
          format);
    if (hasBooleanType(r))
      return format(
          getBooleanValues(r).stream().map(v -> format(v, format)).map(String.class::cast), format);
    return format(
        getStringValues(r).stream().map(v -> format(v, format)).map(String.class::cast), format);
  }

  public static int getValuesCount(Restriction r) {
    if (hasNumberType(r)) return removeNullValues(getNumberValues(r)).size();
    if (hasDateTimeType(r)) return removeNullValues(getDateTimeValues(r)).size();
    if (hasBooleanType(r)) return removeNullValues(getBooleanValues(r)).size();
    return removeNullValues(getStringValues(r)).size();
  }

  private static <T> List<T> removeNullValues(List<T> vals) {
    while (vals.remove(null))
      ;
    return vals;
  }

  public static Map<RestrictionOperator, Value> getInterval(Restriction r) {
    Map<RestrictionOperator, Value> limits = new LinkedHashMap<>();
    if (hasNumberType(r)) {
      NumberRestriction nr = (NumberRestriction) r;
      List<Value> nVals = Val.ofNumber(nr.getValues());
      if (nr.getMinOperator() != null) limits.put(nr.getMinOperator(), nVals.get(0));
      if (nr.getMaxOperator() != null) limits.put(nr.getMaxOperator(), nVals.get(1));
    }
    if (hasDateTimeType(r)) {
      DateTimeRestriction dr = (DateTimeRestriction) r;
      List<Value> dVals = Val.ofDateTime(dr.getValues());
      if (dr.getMinOperator() != null) limits.put(dr.getMinOperator(), dVals.get(0));
      if (dr.getMaxOperator() != null) limits.put(dr.getMaxOperator(), dVals.get(1));
    }
    return limits;
  }

  public static Map<RestrictionOperator, LocalDateTime> getDateTimeInterval(Restriction r) {
    Map<RestrictionOperator, LocalDateTime> limits = new LinkedHashMap<>();
    DateTimeRestriction dr = (DateTimeRestriction) r;
    if (dr.getMinOperator() != null) limits.put(dr.getMinOperator(), dr.getValues().get(0));
    if (dr.getMaxOperator() != null) limits.put(dr.getMaxOperator(), dr.getValues().get(1));
    return limits;
  }

  public static Map<RestrictionOperator, BigDecimal> getNumberInterval(Restriction r) {
    Map<RestrictionOperator, BigDecimal> limits = new LinkedHashMap<>();
    NumberRestriction nr = (NumberRestriction) r;
    if (nr.getMinOperator() != null) limits.put(nr.getMinOperator(), nr.getValues().get(0));
    if (nr.getMaxOperator() != null) limits.put(nr.getMaxOperator(), nr.getValues().get(1));
    return limits;
  }

  public static List<Value> getValues(Restriction r) {
    if (hasNumberType(r)) return Val.ofNumber(getNumberValues(r));
    if (hasDateTimeType(r)) return Val.ofDateTime(getDateTimeValues(r));
    if (hasBooleanType(r)) return Val.ofBoolean(getBooleanValues(r));
    return Val.ofString(getStringValues(r));
  }

  public static List<String> getStringValues(Restriction r) {
    List<String> vals = ((StringRestriction) r).getValues();
    return (vals == null) ? new ArrayList<>() : vals;
  }

  public static List<BigDecimal> getNumberValues(Restriction r) {
    List<BigDecimal> vals = ((NumberRestriction) r).getValues();
    return (vals == null) ? new ArrayList<>() : vals;
  }

  public static List<Boolean> getBooleanValues(Restriction r) {
    List<Boolean> vals = ((BooleanRestriction) r).getValues();
    return (vals == null) ? new ArrayList<>() : vals;
  }

  public static List<LocalDateTime> getDateTimeValues(Restriction r) {
    List<LocalDateTime> vals = ((DateTimeRestriction) r).getValues();
    return (vals == null) ? new ArrayList<>() : vals;
  }

  public static boolean isEntirePeriod(DateTimeRestriction r) {
    return !hasInterval(r) && !hasValues(r);
  }

  public static BigDecimal getMinIntervalValue(NumberRestriction r) {
    if (r.getMinOperator() == null) return null;
    return r.getValues().get(0);
  }

  public static BigDecimal getMaxIntervalValue(NumberRestriction r) {
    if (r.getMaxOperator() == null) return null;
    return r.getValues().get(1);
  }

  private static String format(String val, DataAdapterSettings format) {
    return (format == null) ? val : format.formatString(val);
  }

  private static String format(Boolean val, DataAdapterSettings format) {
    return (format == null) ? val.toString() : format.formatBoolean(val);
  }

  private static String format(BigDecimal val, DataAdapterSettings format) {
    return (format == null) ? val.toPlainString() : format.formatNumber(val);
  }

  private static String format(LocalDateTime val, DataAdapterSettings format) {
    return (format == null) ? DateUtil.format(val) : format.formatDateTime(val);
  }

  private static String format(Stream<String> vals, DataAdapterSettings format) {
    return (format == null)
        ? vals.collect(Collectors.joining(", ", "[", "]"))
        : format.formatList(vals);
  }

  private static String format(RestrictionOperator op, DataAdapterSettings format) {
    return (format == null) ? op.getValue() : format.formatOperator(op);
  }

  public static Restriction getRestriction(List<String> list) {
    RestrictionOperator minOperator = null;
    RestrictionOperator maxOperator = null;
    List<BigDecimal> decimalValues = new ArrayList<>();
    List<LocalDateTime> dateValues = new ArrayList<>();
    List<Boolean> booleanValues = new ArrayList<>();
    List<String> stringValues = new ArrayList<>();

    for (String e : list) {
      if (RestrictionOperator.GREATER_THAN.getValue().equals(e))
        minOperator = RestrictionOperator.GREATER_THAN;
      else if (RestrictionOperator.GREATER_THAN_OR_EQUAL_TO.getValue().equals(e))
        minOperator = RestrictionOperator.GREATER_THAN_OR_EQUAL_TO;
      else if (RestrictionOperator.LESS_THAN.getValue().equals(e))
        maxOperator = RestrictionOperator.LESS_THAN;
      else if (RestrictionOperator.LESS_THAN_OR_EQUAL_TO.getValue().equals(e))
        maxOperator = RestrictionOperator.LESS_THAN_OR_EQUAL_TO;
      else {
        if (NumberUtils.isParsable(e)) decimalValues.add(new BigDecimal(e));
        else {
          Optional<LocalDateTime> dv = DateUtil.parseOptional(e);
          if (dv.isPresent()) dateValues.add(dv.get());
          else if (e.equalsIgnoreCase("true") || e.equalsIgnoreCase("false"))
            booleanValues.add(Boolean.parseBoolean(e));
          else stringValues.add(e);
        }
      }
    }

    if (!decimalValues.isEmpty()) {
      NumberRestriction nr =
          new NumberRestriction().minOperator(minOperator).maxOperator(maxOperator);
      if (minOperator == null && maxOperator != null)
        nr.setValues(List.of(null, decimalValues.get(0)));
      else nr.setValues(decimalValues);
      return nr;
    }
    if (!dateValues.isEmpty()) {
      DateTimeRestriction dr =
          new DateTimeRestriction().minOperator(minOperator).maxOperator(maxOperator);
      if (minOperator == null && maxOperator != null)
        dr.setValues(List.of(null, dateValues.get(0)));
      else dr.setValues(dateValues);
      return dr;
    }
    if (!booleanValues.isEmpty()) return new BooleanRestriction().values(booleanValues);
    return new StringRestriction().values(stringValues);
  }

  public static Restriction convertValues(Restriction r, String inUnit, String outUnit) {
    NumberRestriction nr = (NumberRestriction) r;
    List<BigDecimal> vals =
        nr.getValues().stream()
            .map(v -> UCUM.convert(v, inUnit, outUnit))
            .collect(Collectors.toList());
    return nr.values(vals);
  }

  public static boolean hasExistentialQuantifier(Restriction r) {
    return r.getQuantifier() == Quantifier.MIN && r.getCardinality().intValue() == 1;
  }
}
