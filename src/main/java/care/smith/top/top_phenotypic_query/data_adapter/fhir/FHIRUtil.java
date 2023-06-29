package care.smith.top.top_phenotypic_query.data_adapter.fhir;

import ca.uhn.fhir.context.FhirContext;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;

public class FHIRUtil {

  public static void print(IBaseResource r) {
    System.out.print(toString(r));
  }

  public static String toString(IBaseResource r) {
    return FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(r);
  }

  public static String getId(IBaseResource r) {
    return r.getIdElement().getResourceType() + "/" + r.getIdElement().getIdPart();
  }

  public static String getId(Reference r) {
    return r.getReferenceElement().getResourceType() + "/" + r.getReferenceElement().getIdPart();
  }

  public static String getIdPart(IBaseResource r) {
    return r.getIdElement().getIdPart();
  }

  public static String getIdPart(Reference r) {
    return r.getReferenceElement().getIdPart();
  }

  public static String getString(Base value) {
    if (value == null) return null;
    if (value instanceof Enumeration<?>) return ((Enumeration<?>) value).asStringValue();
    if (value instanceof StringType) return ((StringType) value).getValue();
    if (value instanceof UriType) return ((UriType) value).getValue();
    return null;
  }

  public static LocalDateTime getDateTime(Base value) {
    if (value == null) return null;
    if (value instanceof BaseDateTimeType)
      return DateUtil.ofDate(((BaseDateTimeType) value).getValue());
    return null;
  }

  public static BigDecimal getNumber(Base value) {
    if (value == null) return null;
    if (value instanceof Quantity) return ((Quantity) value).getValue();
    if (value instanceof DecimalType) return ((DecimalType) value).getValue();
    if (value instanceof IntegerType)
      return BigDecimal.valueOf(((IntegerType) value).getValue().longValue());
    return null;
  }

  public static Boolean getBoolean(Base value) {
    if (value == null) return null;
    if (value instanceof BooleanType) return ((BooleanType) value).getValue();
    return null;
  }

  public static DateTimeType parse(String dateTime) {
    return new DateTimeType(DateUtil.parseToDate(dateTime));
  }

  public static Stream<String> codeableConceptToCodeUrisStream(CodeableConcept cc) {
    return cc.getCoding().stream().map(c -> c.getSystem() + "|" + c.getCode());
  }

  public static String codeableConceptToString(CodeableConcept cc, String separator) {
    return codeableConceptToCodeUrisStream(cc).collect(Collectors.joining(separator));
  }
}
