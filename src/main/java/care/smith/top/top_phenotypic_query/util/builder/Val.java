package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.BooleanValue;
import care.smith.top.model.DataType;
import care.smith.top.model.DateTimeValue;
import care.smith.top.model.NumberValue;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.util.Values;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Val {

  public static Value of(Boolean val) {
    return new BooleanValue().value(val).dataType(DataType.BOOLEAN);
  }

  public static Value of(Number val) {
    return new NumberValue().value(Values.toDecimal(val)).dataType(DataType.NUMBER);
  }

  public static Value of(BigDecimal val) {
    return new NumberValue().value(val).dataType(DataType.NUMBER);
  }

  public static Value of(String val) {
    return new StringValue().value(val).dataType(DataType.STRING);
  }

  public static Value of(LocalDateTime val) {
    return new DateTimeValue().value(val).dataType(DataType.DATE_TIME);
  }

  public static Value of(Boolean val, LocalDateTime dateTime) {
    return of(val).dateTime(dateTime);
  }

  public static Value of(Number val, LocalDateTime dateTime) {
    return of(val).dateTime(dateTime);
  }

  public static Value of(BigDecimal val, LocalDateTime dateTime) {
    return of(val).dateTime(dateTime);
  }

  public static Value of(String val, LocalDateTime dateTime) {
    return of(val).dateTime(dateTime);
  }

  public static Value of(LocalDateTime val, LocalDateTime dateTime) {
    return of(val).dateTime(dateTime);
  }

  public static Value of(Boolean val, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(Number val, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(BigDecimal val, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(String val, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(LocalDateTime val, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(
      Boolean val, LocalDateTime dateTime, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(
      Number val, LocalDateTime dateTime, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(
      BigDecimal val,
      LocalDateTime dateTime,
      LocalDateTime starDateTime,
      LocalDateTime endDateTime) {
    return of(val).dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(
      String val, LocalDateTime dateTime, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return of(val).dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value of(
      LocalDateTime val,
      LocalDateTime dateTime,
      LocalDateTime starDateTime,
      LocalDateTime endDateTime) {
    return of(val).dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value ofTrue() {
    return of(true);
  }

  public static Value ofFalse() {
    return of(false);
  }

  public static Value ofTrue(LocalDateTime dateTime) {
    return ofTrue().dateTime(dateTime);
  }

  public static Value ofFalse(LocalDateTime dateTime) {
    return ofFalse().dateTime(dateTime);
  }

  public static Value ofTrue(LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return ofTrue().startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value ofFalse(LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return ofFalse().startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value ofTrue(
      LocalDateTime dateTime, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return ofTrue().dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static Value ofFalse(
      LocalDateTime dateTime, LocalDateTime starDateTime, LocalDateTime endDateTime) {
    return ofFalse().dateTime(dateTime).startDateTime(starDateTime).endDateTime(endDateTime);
  }

  public static List<Value> ofBoolean(List<Boolean> vals) {
    return vals.stream().map(v -> of(v)).collect(Collectors.toList());
  }

  public static List<Value> ofNumber(List<BigDecimal> vals) {
    return vals.stream().map(v -> of(v)).collect(Collectors.toList());
  }

  public static List<Value> ofString(List<String> vals) {
    return vals.stream().map(v -> of(v)).collect(Collectors.toList());
  }

  public static List<Value> ofDateTime(List<LocalDateTime> vals) {
    return vals.stream().map(v -> of(v)).collect(Collectors.toList());
  }

  public static List<Value> of(Number... nums) {
    return Stream.of(nums).map(n -> of(n)).collect(Collectors.toList());
  }
}
