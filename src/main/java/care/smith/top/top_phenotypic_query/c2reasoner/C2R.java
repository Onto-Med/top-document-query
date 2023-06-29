package care.smith.top.top_phenotypic_query.c2reasoner;

import care.smith.top.model.Constant;
import care.smith.top.model.DateTimeRestriction;
import care.smith.top.model.Expression;
import care.smith.top.model.ExpressionFunction;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.ConstantEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.E;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.False;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.GeC;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.GtC;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.LeC;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.LtC;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.Now;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.Pi;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.Today;
import care.smith.top.top_phenotypic_query.c2reasoner.constants.True;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.FunctionEntity;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.If;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.advanced.Switch;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Avg;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Count;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutFirst;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.CutLast;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.First;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Last;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Max;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Median;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.aggregate.Min;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Add;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Divide;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Multiply;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Power;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Subtract;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Sum;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.And;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.MinTrue;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Not;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.bool.Or;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Eq;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Ge;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Gt;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Le;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Lt;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.comparison.Ne;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.Date;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.DiffDays;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.DiffMonths;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.DiffYears;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.EndsBefore;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.Overlap1;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.Overlap2;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusDays;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusMonths;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.PlusYears;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.StartsBefore;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.date_time.TimeDistance;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Empty;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Exists;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Filter;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.In;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Li;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Union;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.Vals;
import care.smith.top.top_phenotypic_query.result.SubjectPhenotypes;
import care.smith.top.top_phenotypic_query.util.Entities;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import care.smith.top.top_phenotypic_query.util.Restrictions;
import care.smith.top.top_phenotypic_query.util.Values;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C2R {

  private MathContext mc = MathContext.DECIMAL64;
  private Map<String, FunctionEntity> functions = new HashMap<>();
  private Map<String, ConstantEntity> constants = new HashMap<>();

  private Entities phenotypes;
  private SubjectPhenotypes values;
  private DateTimeRestriction dateTimeRestriction;
  private FunctionEntity defaultAggregateFunction = Last.get();

  private Logger log = LoggerFactory.getLogger(C2R.class);

  public C2R() {
    init();
  }

  public C2R(boolean withStandardFunctions) {
    if (withStandardFunctions) init();
  }

  private void init() {
    addConstant(Pi.get());
    addConstant(E.get());
    addConstant(True.get());
    addConstant(False.get());
    addConstant(Now.get());
    addConstant(Today.get());
    addConstant(GtC.get());
    addConstant(GeC.get());
    addConstant(LtC.get());
    addConstant(LeC.get());
    addFunction(Add.get());
    addFunction(Subtract.get());
    addFunction(Multiply.get());
    addFunction(Divide.get());
    addFunction(Power.get());
    addFunction(Sum.get());
    addFunction(Avg.get());
    addFunction(Count.get());
    addFunction(First.get());
    addFunction(CutFirst.get());
    addFunction(Last.get());
    addFunction(CutLast.get());
    addFunction(Min.get());
    addFunction(Max.get());
    addFunction(Date.get());
    addFunction(DiffYears.get());
    addFunction(DiffMonths.get());
    addFunction(DiffDays.get());
    addFunction(PlusYears.get());
    addFunction(PlusMonths.get());
    addFunction(PlusDays.get());
    addFunction(And.get());
    addFunction(Or.get());
    addFunction(Not.get());
    addFunction(MinTrue.get());
    addFunction(Eq.get());
    addFunction(Ge.get());
    addFunction(Gt.get());
    addFunction(Le.get());
    addFunction(Lt.get());
    addFunction(Ne.get());
    addFunction(In.get());
    addFunction(Empty.get());
    addFunction(Switch.get());
    addFunction(Filter.get());
    addFunction(Li.get());
    addFunction(If.get());
    addFunction(Median.get());
    addFunction(Overlap1.get());
    addFunction(Overlap2.get());
    addFunction(StartsBefore.get());
    addFunction(EndsBefore.get());
    addFunction(TimeDistance.get());
    addFunction(Exists.get());
    addFunction(Union.get());
    addFunction(Vals.get());
  }

  public MathContext getMathContext() {
    return mc;
  }

  public C2R mathContext(MathContext mc) {
    this.mc = mc;
    return this;
  }

  public Entities getPhenotypes() {
    return phenotypes;
  }

  public C2R phenotypes(Entities phenotypes) {
    this.phenotypes = phenotypes;
    return this;
  }

  public SubjectPhenotypes getValues() {
    return values;
  }

  public C2R values(SubjectPhenotypes values) {
    this.values = values;
    return this;
  }

  public DateTimeRestriction getDateTimeRestriction() {
    return dateTimeRestriction;
  }

  public C2R dateTimeRestriction(DateTimeRestriction dateTimeRestriction) {
    this.dateTimeRestriction = dateTimeRestriction;
    return this;
  }

  public FunctionEntity getDefaultAggregateFunction() {
    return defaultAggregateFunction;
  }

  public C2R defaultAggregateFunction(FunctionEntity defaultAggregateFunction) {
    if (defaultAggregateFunction != null) this.defaultAggregateFunction = defaultAggregateFunction;
    return this;
  }

  public C2R defaultAggregateFunction(String functionId) {
    return defaultAggregateFunction(getFunction(functionId));
  }

  public void addFunction(FunctionEntity function) {
    functions.put(function.getFunction().getId(), function);
  }

  public Set<ExpressionFunction> getExpressionFunctions() {
    return getFunctions().stream().map(f -> f.getFunction()).collect(Collectors.toSet());
  }

  public Collection<FunctionEntity> getFunctions() {
    return functions.values();
  }

  public FunctionEntity getFunction(String id) {
    return functions.get(id);
  }

  public void addConstant(ConstantEntity constant) {
    constants.put(constant.getConstant().getId(), constant);
  }

  public Set<Constant> getExpressionConstants() {
    return getConstants().stream().map(c -> c.getConstant()).collect(Collectors.toSet());
  }

  public Collection<ConstantEntity> getConstants() {
    return constants.values();
  }

  public ConstantEntity getConstant(String id) {
    return constants.get(id);
  }

  public Expression calculate(Phenotype phe) {
    log.debug("start calculating variable: {} ...", phe.getId());

    List<Value> vals = values.getValues(phe.getId(), dateTimeRestriction);
    if (vals != null) return logVariable(phe.getId(), Exp.of(vals), false);

    if (Phenotypes.isSingle(phe) && Phenotypes.hasBooleanType(phe))
      return logVariable(phe.getId(), Exp.ofFalse(), true);

    if (phe.getExpression() == null) return logVariable(phe.getId(), null, false);

    return logVariable(phe.getId(), calculate(phe.getExpression()), true);
  }

  private Expression logVariable(String pheId, Expression res, boolean addToRS) {
    log.debug("end calculating variable: {} = {}", pheId, toString(res));
    if (addToRS && res != null) values.setValues(pheId, dateTimeRestriction, res.getValues());
    return res;
  }

  public Expression calculateVariable(String pheId) {
    return calculate(phenotypes.getPhenotype(pheId));
  }

  public Expression calculate(Expression exp) {
    if (exp == null) return null;
    if (exp.getConstantId() != null) return calculateConstant(exp.getConstantId());
    if (exp.getEntityId() != null) return calculateVariable(exp.getEntityId());
    if (exp.getFunctionId() != null) return calculateFunction(exp);
    return exp;
  }

  private Expression calculateConstant(String constantId) {
    log.debug("start calculating constant: {} ...", constantId);
    Exceptions.checkConstantExists(constantId, constants);
    Expression result = getConstant(constantId).getValueExpression();
    log.debug("end calculating constant: {} = {}", constantId, toString(result));
    return result;
  }

  public Expression calculateFunction(Expression exp) {
    String expStr = toString(exp);
    log.debug("start calculating function '{}': {} ...", exp.getFunctionId(), expStr);
    Exceptions.checkFunctionExists(exp, functions);
    FunctionEntity func = getFunction(exp.getFunctionId());
    Expression result = func.calculate(exp.getArguments(), this);
    log.debug(
        "end calculating function '{}': {} = {}", exp.getFunctionId(), expStr, toString(result));
    return result;
  }

  public List<Expression> calculate(List<Expression> args) {
    List<Expression> calculated = new ArrayList<>();
    for (Expression arg : args) {
      Expression res = calculate(arg);
      if (res == null) return null;
      calculated.add(res);
    }
    return calculated;
  }

  public String toString(Expression exp) {
    if (exp == null) return "null";
    if (exp.getEntityId() != null) return exp.getEntityId();
    if (exp.getConstantId() != null)
      return getConstant(exp.getConstantId()).getConstant().getTitle();
    if (exp.getValues() != null) return Values.toString(exp.getValues());
    if (exp.getRestriction() != null) return Restrictions.toString(exp.getRestriction());
    return functionToString(exp);
  }

  private String functionToString(Expression exp) {
    List<String> args =
        exp.getArguments().stream().map(e -> toString(e)).collect(Collectors.toList());
    return getFunction(exp.getFunctionId()).toString(args);
  }

  public List<Value> getValues(String pheId) {
    Phenotype p = phenotypes.getPhenotype(pheId);
    if (p == null) return null;
    if (Phenotypes.isSinglePhenotype(p)) return values.getValues(pheId, dateTimeRestriction);
    if (Phenotypes.isSingleRestriction(p))
      return values.getValues(Phenotypes.getRestrictedValuesKey(p), dateTimeRestriction);
    return null;
  }
}
