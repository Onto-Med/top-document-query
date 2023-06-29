package care.smith.top.top_phenotypic_query.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import org.fhir.ucum.UcumException;

public class DateUtil {

  private static DateTimeFormatter parseFormatter =
      new DateTimeFormatterBuilder()
          .appendPattern(
              "[yyyy-MM-dd'T'HH:mm:ss][yyyy-MM-dd'T'HH:mm][yyyy-MM-dd][dd.MM.yyyy][dd.MM.yy]")
          .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
          .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
          .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter();

  public static LocalDateTime ofMilli(long milli) {
    return new Timestamp(milli).toLocalDateTime();
  }

  public static LocalDateTime ofDate(Date date) {
    return ofMilli(date.getTime());
  }

  public static long toMilli(LocalDateTime dateTime) {
    return toDate(dateTime).getTime();
  }

  public static Date toDate(LocalDateTime dateTime) {
    return Timestamp.valueOf(dateTime);
  }

  public static LocalDateTime parse(String dateTime) {
    return LocalDateTime.parse(dateTime, parseFormatter);
  }

  public static Date parseToDate(String dateTime) {
    return toDate(parse(dateTime));
  }

  public static String format(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  public static String reformat(String dateTime) {
    return format(parse(dateTime));
  }

  public static String formatDate(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static Optional<LocalDateTime> parseOptional(String dateTime) {
    try {
      return Optional.of(LocalDateTime.parse(dateTime, parseFormatter));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static boolean hasDateTimeFormat(String dateTime) {
    return parseOptional(dateTime).isPresent();
  }

  public static LocalDateTime withoutMilli(LocalDateTime dateTime) {
    if (dateTime == null) return null;
    return dateTime.truncatedTo(ChronoUnit.SECONDS);
  }

  public static LocalDateTime[] parse(String[] datesStr) {
    LocalDateTime[] dates = new LocalDateTime[datesStr.length];
    for (int i = 0; i < datesStr.length; i++) dates[i] = parse(datesStr[i]);
    return dates;
  }

  public static String[] format(LocalDateTime... dates) {
    return Stream.of(dates).map(d -> format(d)).toArray(String[]::new);
  }

  public static String[] formatForName(LocalDateTime... dates) {
    return Stream.of(dates).map(d -> formatForName(d)).toArray(String[]::new);
  }

  public static LocalDateTime getBirthdateForAge(int age, String ucumUnit) throws UcumException {
    LocalDate birthdate = LocalDate.now();
    switch (ucumUnit) {
      case "a":
        birthdate = birthdate.minusYears(age);
        break;
      case "d":
        birthdate = birthdate.minusDays(age);
        break;
      case "mo":
        birthdate = birthdate.minusMonths(age);
        break;
      default:
        throw new UcumException("Invalid date unit!");
    }
    return birthdate.atStartOfDay();
  }

  public static LocalDateTime getBirthdateForAgeInYears(int years) {
    return LocalDate.now().minusYears(years).atStartOfDay();
  }

  public static BigDecimal getPeriod(LocalDateTime start, LocalDateTime end, String ucumUnit)
      throws UcumException {
    switch (ucumUnit) {
      case "a":
        return getPeriodInYears(start, end);
      case "d":
        return getPeriodInDays(start, end);
      case "mo":
        return getPeriodInMonths(start, end);
      default:
        throw new UcumException("Invalid date unit!");
    }
  }

  public static BigDecimal getPeriod(LocalDateTime start, String ucumUnit) throws UcumException {
    return getPeriod(start, LocalDateTime.now(), ucumUnit);
  }

  public static BigDecimal getPeriodInYears(LocalDateTime start, LocalDateTime end) {
    return getPeriodInDays(start, end)
        .divide(new BigDecimal("365.25"), MathContext.DECIMAL32)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal getPeriodInYears(LocalDateTime start) {
    return getPeriodInYears(start, LocalDateTime.now());
  }

  public static BigDecimal getPeriodInMonths(LocalDateTime start, LocalDateTime end) {
    BigDecimal days =
        BigDecimal.valueOf(Period.between(start.toLocalDate(), end.toLocalDate()).getDays());
    BigDecimal months = BigDecimal.valueOf(ChronoUnit.MONTHS.between(start, end));
    return months
        .add(days.divide(BigDecimal.valueOf(30), MathContext.DECIMAL32))
        .setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal getPeriodInDays(LocalDateTime start, LocalDateTime end) {
    return BigDecimal.valueOf(ChronoUnit.DAYS.between(start, end));
  }

  public static BigDecimal getPeriodInHours(LocalDateTime start, LocalDateTime end) {
    return BigDecimal.valueOf(Duration.between(start, end).toMillis())
        .divide(BigDecimal.valueOf(3600000), MathContext.DECIMAL32);
  }
}
