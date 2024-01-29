package care.smith.top.top_document_query.util.builder;

import care.smith.top.model.DataType;
import care.smith.top.model.NumberValue;
import care.smith.top.model.StringValue;
import care.smith.top.model.Value;
import care.smith.top.top_document_query.util.Values;

public class Val {
  public static Value of(Number val) {
    return new NumberValue().value(Values.toDecimal(val)).dataType(DataType.NUMBER);
  }

  public static Value of(String val) {
    return new StringValue().value(val).dataType(DataType.STRING);
  }
}
