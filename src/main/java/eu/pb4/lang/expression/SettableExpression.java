package eu.pb4.lang.expression;

public interface SettableExpression extends Expression {
    Expression asSetter(Expression value);
    Expression asSetterWithOldReturn(Expression value);
}
