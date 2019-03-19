package yanry.lib.java.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author yanry
 *
 *         2015年12月29日
 */
public class StringMatcher {
	private List<Object> operandList;
	private Map<String, Boolean> regexMap;
	private Map<String, OnViolationListener> intersectionListeners;
	private OnViolationListener unionListener;

	/**
	 * Intersection.
	 * 
	 */
	public StringMatcher() {
		operandList = new LinkedList<Object>();
		regexMap = new HashMap<String, Boolean>();
		intersectionListeners = new HashMap<String, StringMatcher.OnViolationListener>();
	}

	/**
	 * Union.
	 * 
	 * @param unionListener
	 */
	public StringMatcher(OnViolationListener unionListener) {
		operandList = new LinkedList<Object>();
		regexMap = new HashMap<String, Boolean>();
		this.unionListener = unionListener;
	}

	public boolean test(String input) {
		boolean union = intersectionListeners == null;
		for (Object op : operandList) {
			if (op instanceof String) {
				String ex = (String) op;
				boolean negation = regexMap.get(ex);
				if (!union ^ (negation ? !input.matches(ex) : input.matches(ex))) {
					if (!union) {
						OnViolationListener listener = intersectionListeners.get(ex);
						if (listener != null) {
							listener.onViolate();
						}
					}
					return union;
				}
			} else {
				StringMatcher sm = (StringMatcher) op;
				if (!union ^ sm.test(input)) {
					return union;
				}
			}
		}
		if (union && unionListener != null) {
			unionListener.onViolate();
		}
		return !union;
	}

	public StringMatcher addMatcher(StringMatcher matcher) {
		operandList.add(matcher);
		return this;
	}

	/**
	 * 
	 * @param regex
	 * @param negation
	 *            取反，即正则表达式匹配时返回false，否则返回true。
	 * @param listener
	 *            this parameter will be ignored when using union constructor.
	 * @return
	 */
	public StringMatcher regularExpression(String regex, boolean negation, OnViolationListener listener) {
		operandList.add(regex);
		regexMap.put(regex, negation);
		if (listener != null && intersectionListeners != null) {
			intersectionListeners.put(regex, listener);
		}
		return this;
	}

	public StringMatcher noWhitespace(boolean negation, OnViolationListener listener) {
		return regularExpression("^[^\\s]*$", negation, listener);
	}

	public StringMatcher chineseChars(boolean all, boolean negation, OnViolationListener listener) {
		return regularExpression(all ? "[\u4e00-\u9fa5]+" : ".*[\u4e00-\u9fa5]+.*", negation, listener);
	}

	public StringMatcher length(int min, int max, OnViolationListener listener) {
		return regularExpression(
				String.format(".{%s,%s}", min, max), false,
				listener);
	}

	public StringMatcher email(OnViolationListener listener) {
		return regularExpression(
				"[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?",
				false, listener);
	}

	/**
	 * [区号(3~4)[-]]座机号(7~8)
	 * 
	 * @param listener
	 * @return
	 */
	public StringMatcher phone(OnViolationListener listener) {
		return regularExpression("^(\\d{3}-?)?\\d{8}|(\\d{4}-?)?\\d{7,8}$", false, listener);
	}

	public StringMatcher mobile(OnViolationListener listener) {
		return regularExpression("^1[1-9]\\d{9}$", false, listener);
	}

	public StringMatcher url(OnViolationListener listener) {
		return regularExpression("[a-zA-z]+://[\\S]*", false, listener);
	}

	public StringMatcher postCode(OnViolationListener listener) {
		return regularExpression("[1-9]\\d{5}(?!\\d)", false, listener);
	}

	public StringMatcher idCardNo(OnViolationListener listener) {
		return regularExpression("^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$", false, listener);
	}

	public StringMatcher qq(OnViolationListener listener) {
		return regularExpression("[1-9][0-9]{4,}", false, listener);
	}

	public StringMatcher integer(boolean positive, boolean includeZero, OnViolationListener listener) {
		return regularExpression(
				String.format(positive ? "^([1-9]\\d*)%s$" : "^(-[1-9]\\d*)%s$", includeZero ? "|0" : ""), false,
				listener);
	}

	public StringMatcher floatNum(boolean positive, OnViolationListener listener) {
		return regularExpression(
				positive ? "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$" : "^-[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$", false,
				listener);
	}

	public interface OnViolationListener {
		void onViolate();
	}
}
