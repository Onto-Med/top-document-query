package care.smith.top.top_document_query.util.builder;

import care.smith.top.model.Entity;
import care.smith.top.model.Expression;
import care.smith.top.model.Value;
import care.smith.top.top_document_query.util.builder.Val;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Exp {
    public static Expression of(Value val) {
        return new Expression().addValuesItem(val);
    }
    public static Expression of(Entity p) {
        return ofEntity(p.getId());
    }
    public static Expression of(Number val) {
        return of(Val.of(val));
    }
    public static Expression of(List<Value> vals) {
        return new Expression().values(vals);
    }
    public static Expression of(String val) {
        return of(Val.of(val));
    }

    public static Expression ofEntity(String entityId) {
        return new Expression().entityId(entityId);
    }

    public static List<Expression> toList(Entity... entities) {
        return Stream.of(entities).map(e -> of(e)).collect(Collectors.toList());
    }

    public static Expression function(String functionId, List<Expression> args) {
        return new Expression().functionId(functionId).arguments(args);
    }
    public static Expression function(String functionId, Expression arg) {
        return new Expression().functionId(functionId).addArgumentsItem(arg);
    }
    public static Expression function(String functionId, Expression... args) {
        return function(functionId, List.of(args));
    }
}
