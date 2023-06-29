package care.smith.top.top_phenotypic_query.song;

import care.smith.top.model.*;
import care.smith.top.top_phenotypic_query.song.functions.And;
import care.smith.top.top_phenotypic_query.song.functions.Dist;
import care.smith.top.top_phenotypic_query.song.functions.Not;
import care.smith.top.top_phenotypic_query.song.functions.Or;
import care.smith.top.top_phenotypic_query.song.functions.SubTree;
import care.smith.top.top_phenotypic_query.song.functions.TextFunction;
import care.smith.top.top_phenotypic_query.song.functions.XProd;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Expressions;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SONG {
  private final Logger log = LoggerFactory.getLogger(SONG.class);
  private final Map<String, TextFunction> functions = new HashMap<>();

  private Entities concepts;
  private String lang;

  public static final String EXPRESSION_TYPE_QUERY = "query";
  public static final String EXPRESSION_TYPE_TERMS_INITIAL = "terms_initial";
  public static final String EXPRESSION_TYPE_TERMS_PROCESSED = "terms_processed";

  protected SONG(And and, Or or, Not not, Dist dist) {
    addFunction(and);
    addFunction(or);
    addFunction(not);
    addFunction(dist);
    addFunction(SubTree.get());
    addFunction(XProd.get());
  }

  public Entities getConcepts() {
    return concepts;
  }

  public Concept getConcept(String id) {
    return concepts.getConcept(id);
  }

  public void setConcepts(Entities concepts) {
    this.concepts = concepts;
  }

  public static ExpressionFunction[] getExpressionFunctions() {
    return new ExpressionFunction[] {
      And.FUNCTION, Or.FUNCTION, Not.FUNCTION, Dist.FUNCTION, SubTree.FUNCTION, XProd.FUNCTION
    };
  }

  private void addFunction(TextFunction f) {
    functions.put(f.getId(), f);
  }

  public Collection<TextFunction> getFunctions() {
    return functions.values();
  }

  public TextFunction getFunction(String id) {
    return functions.get(id);
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public Expression generate(Concept con) {
    log.debug("start generating query for concept: {} ...", con.getId());

    if (con instanceof CompositeConcept) {
      Expression res = generate(((CompositeConcept) con).getExpression());
      log.debug("end generating query for concept : {} = {}", con.getId(), toString(res));
      return res;
    }

    Expression res = getTermsExpression(con, EXPRESSION_TYPE_TERMS_INITIAL, false);
    log.debug("end generating query for concept: {} = {}", con.getId(), toString(res));
    return res;
  }

  public Expression generate(Expression exp) {
    if (exp.getEntityId() != null) return generate(exp.getEntityId());
    if (exp.getFunctionId() != null) return generateFunction(exp);
    return exp;
  }

  public Expression generate(String conId) {
    return generate(getConcept(conId));
  }

  public Expression generateFunction(Expression exp) {
    String expStr = toString(exp);
    log.debug("start generating query for function '{}': {} ...", exp.getFunctionId(), expStr);
    TextFunction func = getFunction(exp.getFunctionId());
    Expression res = func.generate(exp.getArguments(), this);
    log.debug(
        "end generating query for function '{}': {} = {}",
        exp.getFunctionId(),
        expStr,
        toString(res));
    return res;
  }

  public List<Expression> generate(List<Expression> args) {
    return args.stream()
        .map(this::generate)
        .filter(a -> !Expressions.isEmpty(a))
        .collect(Collectors.toList());
  }

  public String getQuery(Expression exp) {
    if (Expressions.hasQuery(exp)) return Expressions.getStringValue(exp);
    return getTermsQuery(exp);
  }

  public Expression getTermsExpression(String conId, String type, boolean includeSubTree) {
    return getTermsExpression(getConcept(conId), type, includeSubTree);
  }

  public Expression getTermsExpression(Entity con, String type, boolean includeSubTree) {
    Set<String> terms = Entities.getTerms(con, lang, includeSubTree);
    if (terms.isEmpty()) return new Expression();
    return Exp.of(terms.stream().map(Val::of).collect(Collectors.toList())).type(type);
  }

  public Expression getTermsQueryExpression(List<String> terms) {
    List<Expression> args = terms.stream().map(Exp::of).collect(Collectors.toList());
    return getFunction(Or.ID).generate(args, this);
  }

  private String getTermsQuery(Expression exp) {
    List<Expression> args =
        exp.getValues().stream().map(v -> Exp.of(getTerm(v, exp))).collect(Collectors.toList());
    if (args.size() == 1) return Expressions.getStringValue(args.get(0));
    return Expressions.getStringValue(getFunction(Or.ID).generate(args, this));
  }

  public String getTerm(Value val, Expression exp) {
    String term = Values.getStringValue(val);
    if (Expressions.hasTermsInitial(exp)) return quotePhrase(term);
    return term;
  }

  private String quotePhrase(String s) {
    if (StringUtils.containsWhitespace(s)) return "\"" + s + "\"";
    return s;
  }

  public String toString(Expression exp) {
    if (exp == null) return "null";
    if (exp.getEntityId() != null) return exp.getEntityId();
    if (exp.getValues() != null) return Values.toString(exp.getValues());
    if (exp.getRestriction() != null) return Restrictions.toString(exp.getRestriction());
    return operatorToString(exp);
  }

  private String operatorToString(Expression exp) {
    List<String> args =
        exp.getArguments().stream().map(this::toString).collect(Collectors.toList());
    return getFunction(exp.getFunctionId()).toString(args);
  }
}
