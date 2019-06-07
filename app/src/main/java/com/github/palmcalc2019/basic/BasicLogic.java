/**
 * <Palmcalc is a multipurpose application consisting of calculators, converters
 * and world clock> Copyright (C) <2013> <Cybrosys Technologies pvt. ltd.>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package com.github.palmcalc2019.basic;

import com.github.palmcalc2019.ui.CalculatorDisplay;
import com.github.palmcalc2019.ui.History;
import com.github.palmcalc2019.ui.Logic;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.content.Context;

import java.util.Locale;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

import androidx.core.content.PermissionChecker;

class BasicLogic extends Logic {
	private CalculatorDisplay mDisplay;
	private Symbols mSymbols = new Symbols();
	private History mHistory;
	private String mResult = "";
	private boolean mIsError = false;
	private int mLineLength = 0;

	private static final String INFINITY_UNICODE = "\u221e";

	public static final String MARKER_EVALUATE_ON_RESUME = "?";
	private static final String INFINITY = "Infinity";
	private static final String NAN = "NaN";

	private final String mErrorString;
	public final static int DELETE_MODE_BACKSPACE = 0;
	public final static int DELETE_MODE_CLEAR = 1;
	private int mDeleteMode = DELETE_MODE_BACKSPACE;

	public interface Listener {
		void onDeleteModeChange();
	}

	private Listener mListener;

	BasicLogic(Context context, History history, CalculatorDisplay display) {
		mErrorString = "Error";
		mHistory = history;
		mDisplay = display;
		mDisplay.setLogic(this);
	}

	public void setListener(Listener listener) {
		this.mListener = listener;
	}

	public void setDeleteMode(int mode) {
		if (mDeleteMode != mode) {
			mDeleteMode = mode;
			mListener.onDeleteModeChange();
		}
	}

	public int getDeleteMode() {
		return mDeleteMode;
	}

	public String getDisplayText() {
		return mDisplay.getText().toString();
	}

	void setLineLength(int nDigits) {
		mLineLength = nDigits;
	}

	boolean eatHorizontalMove(boolean toLeft) {
		EditText editText = mDisplay.getEditText();
		int cursorPos = editText.getSelectionStart();
		return toLeft ? cursorPos == 0 : cursorPos >= editText.length();
	}

	private String getText() {
		return mDisplay.getText().toString();
	}

	@Override
	public void insert(String delta) {
		Log.d("Display", delta);
		mDisplay.insert(delta);
		setDeleteMode(DELETE_MODE_BACKSPACE);
	}

	public void resumeWithHistory() {
		clearWithHistory(false);
	}

	private void clearWithHistory(boolean scroll) {
		String text = mHistory.getText();
		if (MARKER_EVALUATE_ON_RESUME.equals(text)) {
			if (!mHistory.moveToPrevious()) {
				text = "";
			}
			text = mHistory.getText();
			evaluateAndShowResult(text, CalculatorDisplay.Scroll.NONE);
		} else {
			mResult = "";
			mDisplay.setText(text, scroll ? CalculatorDisplay.Scroll.UP
					: CalculatorDisplay.Scroll.NONE);
			mIsError = false;
		}
	}

	private void clear(boolean scroll) {
		mHistory.enter("");
		mDisplay.setText("", scroll ? CalculatorDisplay.Scroll.UP
				: CalculatorDisplay.Scroll.NONE);
		cleared();
	}

	@Override
	public void cleared() {
		mResult = "";
		mIsError = false;
		updateHistory();

		setDeleteMode(DELETE_MODE_BACKSPACE);
	}

	@Override
	public boolean acceptInsert(String delta) {
		String text = getText();
		return !mIsError
				&& (!mResult.equals(text) || isOperator(delta) || mDisplay
						.getSelectionStart() != text.length());
	}

	@Override
	public void onDelete() {
		if (getText().equals(mResult) || mIsError) {
			clear(false);
		} else {
			mDisplay.dispatchKeyEvent(new KeyEvent(0, KeyEvent.KEYCODE_DEL));
			mResult = "";
		}
	}

	void onClear() {
		clear(mDeleteMode == DELETE_MODE_CLEAR);
	}

	@Override
	public void onEnter() {
		if (mDeleteMode == DELETE_MODE_CLEAR) {
			clearWithHistory(false); // clear after an Enter on result
		} else {
			evaluateAndShowResult(getText(), CalculatorDisplay.Scroll.UP);
		}
	}

	public void evaluateAndShowResult(String text, CalculatorDisplay.Scroll scroll) {
		try {
			String result = evaluate(text);
			Log.d("RESULT", result);
			if (!text.equals(result)) {
				mHistory.enter(text);
				mResult = result;
				mDisplay.setText(mResult, scroll);
			}
		} catch (SyntaxException e) {
			mIsError = true;
			mResult = mErrorString;
			mDisplay.setText(mResult, scroll);
		}
	}

	void onUp() {
		String text = getText();
		if (!text.equals(mResult)) {
			mHistory.update(text);
		}
		if (mHistory.moveToPrevious()) {
			mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.DOWN);
		}
	}

	void onDown() {
		String text = getText();
		if (!text.equals(mResult)) {
			mHistory.update(text);
		}
		if (mHistory.moveToNext()) {
			mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.UP);
		}
	}

	void updateHistory() {
		String text = getText();
		// Don't set the ? marker for empty text or the error string.
		// There is no need to evaluate those later.
		if (!TextUtils.isEmpty(text) && !TextUtils.equals(text, mErrorString)
				&& text.equals(mResult)) {
			mHistory.update(MARKER_EVALUATE_ON_RESUME);
		} else {
			mHistory.update(getText());
		}
	}

	@Override
	public String evaluate(String input) throws SyntaxException {
		if (input.trim().equals("")) {
			return "";
		}
		int size = input.length();
		while (size > 0 && isOperator(input.charAt(size - 1))) {
			input = input.substring(0, size - 1);
			--size;
		}

		double value = mSymbols.eval(input);

		String result = "";
		for (int precision = mLineLength; precision > 6; precision--) {
			result = tryFormattingWithPrecision(value, precision);
			if (result.length() <= mLineLength) {
				break;
			}
		}
		return result.replace('-', chMinus).replace(INFINITY, INFINITY_UNICODE);
	}

	private String tryFormattingWithPrecision(double value, int precision) {
		// The standard scientific formatter is basically what we need. We will
		// start with what it produces and then massage it a bit.
		String result = String.format(Locale.US, "%" + mLineLength + "."
				+ precision + "g", value);
		if (result.equals(NAN)) { // treat NaN as Error
			mIsError = true;
			return mErrorString;
		}
		String mantissa = result;
		String exponent = null;
		int e = result.indexOf('e');
		if (e != -1) {
			mantissa = result.substring(0, e);

			// Strip "+" and unnecessary 0's from the exponent
			exponent = result.substring(e + 1);
			if (exponent.startsWith("+")) {
				exponent = exponent.substring(1);
			}
			exponent = String.valueOf(Integer.parseInt(exponent));
		} else {
			mantissa = result;
		}

		int period = mantissa.indexOf('.');
		if (period == -1) {
			period = mantissa.indexOf(',');
		}
		if (period != -1) {
			// Strip trailing 0's
			while (mantissa.length() > 0 && mantissa.endsWith("0")) {
				mantissa = mantissa.substring(0, mantissa.length() - 1);
			}
			if (mantissa.length() == period + 1) {
				mantissa = mantissa.substring(0, mantissa.length() - 1);
			}
		}

		if (exponent != null) {
			result = mantissa + 'e' + exponent;
		} else {
			result = mantissa;
		}
		return result;
	}

	@Override
	public boolean isOperator(char c) {
		// plus minus times div
		return "+-\u00d7\u00f7/*".indexOf(c) != -1;
	}
}