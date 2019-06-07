package com.github.palmcalc2019.ui;

import org.javia.arity.SyntaxException;

/**
 * @author Adam Howard
 * @since 2019-06-07
 */
public abstract class Logic {
    public abstract void insert(String string);
    public abstract void onEnter();
    public abstract boolean isOperator(char c);
    public abstract String evaluate(String expr) throws SyntaxException;
    public abstract void cleared();
    public abstract boolean acceptInsert(String s);
}
