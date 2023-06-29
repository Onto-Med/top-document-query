package care.smith.top.top_phenotypic_query.data_adapter.config;

import java.util.Map;

public class QueryBuilder {

  private StringBuffer queryString = new StringBuffer();

  protected QueryBuilder() {}

  protected void add(String queryPart) {
    queryString.append(queryPart);
  }

  protected void add(String queryPart, Map<String, String> map) {
    add(replace(queryPart, map));
  }

  protected void add(String queryPart, String values) {
    add(queryPart.replace(Props.VAR_VALUES, values));
  }

  protected void add(String queryPart, String operator, String value) {
    add(queryPart.replace(Props.VAR_OPERATOR, operator).replace(Props.VAR_VALUE, value));
  }

  protected void add(String queryPart, String values, Map<String, String> map) {
    add(queryPart.replace(Props.VAR_VALUES, values), map);
  }

  protected void add(String queryPart, String operator, String value, Map<String, String> map) {
    add(queryPart.replace(Props.VAR_OPERATOR, operator).replace(Props.VAR_VALUE, value), map);
  }

  protected void addCodes(String queryPart, String codes, Map<String, String> map) {
    add(queryPart.replace(Props.VAR_CODES, codes), map);
  }

  public static String replace(String s, Map<String, String> map) {
    if (map == null) return s;
    for (String key : map.keySet())
      s = s.replace(Props.OPEN_BRACKET + key + Props.CLOSED_BRACKET, map.get(key));
    return s;
  }

  public String build() {
    return queryString.toString();
  }
}
