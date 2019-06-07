package com.github.palmcalc2019.ui;

import org.javia.arity.SyntaxException;

/**
 * @author Adam Howard
 * @since 2019-06-07
 */
public interface Logic {
    void insert(String string);
    void onEnter();
    boolean isOperator(char c);
    String evaluate(String expr) throws SyntaxException;
    void cleared();
    boolean acceptInsert(String s);
}
