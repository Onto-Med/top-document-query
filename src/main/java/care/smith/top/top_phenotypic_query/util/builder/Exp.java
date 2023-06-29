package care.smith.top.top_phenotypic_query.util.builder;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.Phenotype;
import care.smith.top.model.Restriction;
import care.smith.top.model.Value;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.set.In;
import care.smith.top.top_phenotypic_query.util.Phenotypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Exp {

  public static Expression of(Entity p) {
    return ofEntity(p.getId());
  }

  public static Expression ofEntity(String entityId) {
    return new Expression().entityId(entityId);
  }

  public static Expression ofConstant(String constantId) {
    return new Expression().constantId(constantId);
  }

  public static Expression of(Restriction r) {
    return new Expression().restriction(r);
  }

  public static Expression of(Value val) {
    return new Expression().addValuesItem(val);
  }

  public static Expression of(List<Value> vals) {
    return new Expression().values(vals);
  }

  public static Expression of(Value... vals) {
    return of(List.of(vals));
  }

  public static Expression of(Number val, LocalDateTime dateTime) {
    return of(Val.of(val, dateTime));
  }

  public static Expression of(Number val) {
    return of(Val.of(val));
  }

  public static Expression of(String val) {
    return of(Val.of(val));
  }

  public static Expression of(Number... numbers) {
    return of(Val.of(numbers));
  }

  public static Expression of(Boolean val) {
    return of(Val.of(val));
  }

  public static Expression of(LocalDateTime val) {
    return of(Val.of(val));
  }

  public static List<Expression> toList(List<Value> vals) {
    return vals.stream().map(v -> of(v)).collect(Collectors.toList());
  }

  public static Expression[] toArray(Entity... entities) {
    return Stream.of(entities).map(e -> of(e)).toArray(Expression[]::new);
  }

  public static List<Expression> toList(Entity... entities) {
    return Stream.of(entities).map(e -> of(e)).collect(Collectors.toList());
  }

  public static Expression ofTrue() {
    return of(Val.ofTrue());
  }

  public static Expression ofFalse() {
    return of(Val.ofFalse());
  }

  public static Expression inRestriction(Phenotype p) {
    if (!Phenotypes.isRestriction(p)) return null;
    return inRestriction(p.getSuperPhenotype(), p.getRestriction());
  }

  public static Expression inRestriction(Phenotype superPhe, Restriction r) {
    if (r == null) return null;
    return In.of(of(superPhe), of(r));
  }

  public static Expression function(String functionId, List<Expression> args) {
    return new Expression().functionId(functionId).arguments(args);
  }

  public static Expression function(String functionId, Expression... args) {
    return function(functionId, List.of(args));
  }

  public static Expression function(String functionId, Expression arg) {
    return new Expression().functionId(functionId).addArgumentsItem(arg);
  }
}
