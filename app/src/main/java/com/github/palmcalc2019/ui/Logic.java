package com.github.palmcalc2019.ui;

import org.javia.arity.SyntaxException;

/**
 * @author Adam Howard
 * @since 2019-06-07
 */
public abstract class Logic {
    public static final char chMinus = '-';

    public abstract void insert(String string);
    public abstract void onEnter();
    public abstract boolean isOperator(char c);
    public abstract String evaluate(String expr) throws SyntaxException;
    public abstract void cleared();
    public abstract boolean acceptInsert(String s);

    public abstract void onDelete();

    public boolean isOperator(String strText) {
        return strText.length() == 1 && isOperator(strText.charAt(0));
    }
}
