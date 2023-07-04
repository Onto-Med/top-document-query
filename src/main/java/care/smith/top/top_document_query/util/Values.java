package care.smith.top.top_document_query.util;

import care.smith.top.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Values {
    public static BigDecimal toDecimal(Number num) {
        if (num == null) return null;
        return new BigDecimal(num.toString());
    }

    private static String addDateTime(String str, LocalDateTime dateTime) {
        return (dateTime == null) ? str : str + "|" + DateUtil.format(dateTime);
    }

    public static String toString(Value val) {
        return addDateTime(toStringWithoutDateTime(val), val.getDateTime());
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
}
