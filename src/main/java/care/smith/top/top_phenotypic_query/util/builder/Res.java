package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.NumberRestriction;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Quantifier;
import care.smith.top.model.RestrictionOperator;
import care.smith.top.model.StringRestriction;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Values;
import java.time.LocalDateTime;
import java.util.List;

public class Res {

  public static StringRestriction of(String... vals) {
    StringRestriction sr =
        (StringRestriction)
            new StringRestriction().quantifier(Quantifier.MIN).cardinality(1).type(DataType.STRING);
    for (String val : vals) sr.addValuesItem(val);
    return sr;
  }

  public static StringRestriction ofCodes(Phenotype p) {
    return (StringRestriction)
        new StringRestriction()
            .values(Phenotypes.getCodeUris(p))
            .quantifier(Quantifier.MIN)
            .cardinality(1)
            .type(DataType.STRING);
  }

  public static NumberRestriction of(Integer card, Quantifier quan, Number... vals) {
    return (NumberRestriction)
        new NumberRestriction()
            .values(Values.toDecimals(vals))
            .quantifier(quan)
            .cardinality(card)
            .type(DataType.NUMBER);
  }

  public static NumberRestriction of(Number... vals) {
    return of(1, Quantifier.MIN, vals);
  }

  public static NumberRestriction of(
      Integer card, Quantifier quan, RestrictionOperator oper, Number val) {
    NumberRestriction r = new NumberRestriction();
    if (oper == RestrictionOperator.GREATER_THAN
        || oper == RestrictionOperator.GREATER_THAN_OR_EQUAL_TO)
      r.minOperator(oper).addValuesItem(Values.toDecimal(val));
    else if (oper == RestrictionOperator.LESS_THAN
        || oper == RestrictionOperator.LESS_THAN_OR_EQUAL_TO)
      r.maxOperator(oper).addValuesItem(null).addValuesItem(Values.toDecimal(val));
    return (NumberRestriction) r.quantifier(quan).cardinality(card).type(DataType.NUMBER);
  }

  public static NumberRestriction of(RestrictionOperator oper, Number val) {
    return of(1, Quantifier.MIN, oper, val);
  }

  public static NumberRestriction ge(Number val) {
    return of(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO, val);
  }

  public static NumberRestriction gt(Number val) {
    return of(RestrictionOperator.GREATER_THAN, val);
  }

  public static NumberRestriction le(Number val) {
    return of(RestrictionOperator.LESS_THAN_OR_EQUAL_TO, val);
  }

  public static NumberRestriction lt(Number val) {
    return of(RestrictionOperator.LESS_THAN, val);
  }

  public static NumberRestriction of(
      Integer card,
      Quantifier quan,
      RestrictionOperator minOper,
      Number minVal,
      RestrictionOperator maxOper,
      Number maxVal) {
    return (NumberRestriction)
        new NumberRestriction()
            .minOperator(minOper)
            .maxOperator(maxOper)
            .addValuesItem(Values.toDecimal(minVal))
            .addValuesItem(Values.toDecimal(maxVal))
            .quantifier(quan)
            .cardinality(card)
            .type(DataType.NUMBER);
  }

  public static NumberRestriction of(
      RestrictionOperator minOper, Number minVal, RestrictionOperator maxOper, Number maxVal) {
    return of(1, Quantifier.MIN, minOper, minVal, maxOper, maxVal);
  }

  public static NumberRestriction gtLt(Number min, Number max) {
    return of(RestrictionOperator.GREATER_THAN, min, RestrictionOperator.LESS_THAN, max);
  }

  public static NumberRestriction geLe(Number min, Number max) {
    return of(
        RestrictionOperator.GREATER_THAN_OR_EQUAL_TO,
        min,
        RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
        max);
  }

  public static NumberRestriction gtLe(Number min, Number max) {
    return of(
        RestrictionOperator.GREATER_THAN, min, RestrictionOperator.LESS_THAN_OR_EQUAL_TO, max);
  }

  public static NumberRestriction geLt(Number min, Number max) {
    return of(
        RestrictionOperator.GREATER_THAN_OR_EQUAL_TO, min, RestrictionOperator.LESS_THAN, max);
  }

  public static NumberRestriction of(Quantifier quan, Number... vals) {
    return of(null, quan, vals);
  }

  public static NumberRestriction of(Quantifier quan, RestrictionOperator oper, Number val) {
    return of(null, quan, oper, val);
  }

  public static NumberRestriction of(
      Quantifier quan,
      RestrictionOperator minOper,
      Number minVal,
      RestrictionOperator maxOper,
      Number maxVal) {
    return of(null, quan, minOper, minVal, maxOper, maxVal);
  }

  public static DateTimeRestriction of(Integer card, Quantifier quan, LocalDateTime... vals) {
    return (DateTimeRestriction)
        new DateTimeRestriction()
            .values(List.of(vals))
            .quantifier(quan)
            .cardinality(card)
            .type(DataType.DATE_TIME);
  }

  public static DateTimeRestriction of(LocalDateTime... vals) {
    return of(1, Quantifier.MIN, vals);
  }

  public static DateTimeRestriction of(
      Integer card, Quantifier quan, RestrictionOperator oper, LocalDateTime val) {
    DateTimeRestriction r = new DateTimeRestriction();
    if (oper == RestrictionOperator.GREATER_THAN
        || oper == RestrictionOperator.GREATER_THAN_OR_EQUAL_TO)
      r.minOperator(oper).addValuesItem(val);
    else if (oper == RestrictionOperator.LESS_THAN
        || oper == RestrictionOperator.LESS_THAN_OR_EQUAL_TO)
      r.maxOperator(oper).addValuesItem(null).addValuesItem(val);
    return (DateTimeRestriction) r.quantifier(quan).cardinality(card).type(DataType.DATE_TIME);
  }

  public static DateTimeRestriction of(
      Integer card,
      Quantifier quan,
      RestrictionOperator minOper,
      LocalDateTime minVal,
      RestrictionOperator maxOper,
      LocalDateTime maxVal) {
    return (DateTimeRestriction)
        new DateTimeRestriction()
            .minOperator(minOper)
            .maxOperator(maxOper)
            .addValuesItem(minVal)
            .addValuesItem(maxVal)
            .quantifier(quan)
            .cardinality(card)
            .type(DataType.DATE_TIME);
  }

  public static DateTimeRestriction of(Quantifier quan, LocalDateTime... vals) {
    return of(null, quan, vals);
  }

  public static DateTimeRestriction of(
      Quantifier quan, RestrictionOperator oper, LocalDateTime val) {
    return of(null, quan, oper, val);
  }

  public static DateTimeRestriction of(
      Quantifier quan,
      RestrictionOperator minOper,
      LocalDateTime minVal,
      RestrictionOperator maxOper,
      LocalDateTime maxVal) {
    return of(null, quan, minOper, minVal, maxOper, maxVal);
  }

  public static DateTimeRestriction of(RestrictionOperator oper, LocalDateTime val) {
    return of(1, Quantifier.MIN, oper, val);
  }

  public static DateTimeRestriction ge(LocalDateTime val) {
    return of(RestrictionOperator.GREATER_THAN_OR_EQUAL_TO, val);
  }

  public static DateTimeRestriction gt(LocalDateTime val) {
    return of(RestrictionOperator.GREATER_THAN, val);
  }

  public static DateTimeRestriction le(LocalDateTime val) {
    return of(RestrictionOperator.LESS_THAN_OR_EQUAL_TO, val);
  }

  public static DateTimeRestriction lt(LocalDateTime val) {
    return of(RestrictionOperator.LESS_THAN, val);
  }

  public static DateTimeRestriction of(
      RestrictionOperator minOper,
      LocalDateTime minVal,
      RestrictionOperator maxOper,
      LocalDateTime maxVal) {
    return of(1, Quantifier.MIN, minOper, minVal, maxOper, maxVal);
  }

  public static DateTimeRestriction gtLt(LocalDateTime min, LocalDateTime max) {
    return of(RestrictionOperator.GREATER_THAN, min, RestrictionOperator.LESS_THAN, max);
  }

  public static DateTimeRestriction geLe(LocalDateTime min, LocalDateTime max) {
    return of(
        RestrictionOperator.GREATER_THAN_OR_EQUAL_TO,
        min,
        RestrictionOperator.LESS_THAN_OR_EQUAL_TO,
        max);
  }

  public static DateTimeRestriction gtLe(LocalDateTime min, LocalDateTime max) {
    return of(
        RestrictionOperator.GREATER_THAN, min, RestrictionOperator.LESS_THAN_OR_EQUAL_TO, max);
  }

  public static DateTimeRestriction geLt(LocalDateTime min, LocalDateTime max) {
    return of(
        RestrictionOperator.GREATER_THAN_OR_EQUAL_TO, min, RestrictionOperator.LESS_THAN, max);
  }
}
