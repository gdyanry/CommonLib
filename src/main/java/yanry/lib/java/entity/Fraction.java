package yanry.lib.java.entity;

import yanry.lib.java.util.MathUtil;

/**
 * 把小数转化为以10的指数为分母的分数（约分前）。
 * 
 * @author yanry
 *
 *         2015年5月14日 上午9:13:55
 */
public class Fraction {

	private int numerator;
	private int denominator;
	private boolean reduced;

	public Fraction(double f) {
		this(String.valueOf(f));
	}

	/**
	 * 
	 * @param expression string presentation of a float number.
	 */
	public Fraction(String expression) {
		String[] arr = expression.split("\\.");
		int intPart = Integer.parseInt(arr[0]);
		int numerPart = 0;
		denominator = 1;
		if (arr.length == 2) {
			numerPart = Integer.parseInt(arr[1]);
			denominator = 10;
			while (numerPart / denominator > 0) {
				denominator *= 10;
			}
		}
		numerator = intPart * denominator + numerPart;
	}

	/**
	 * 
	 * @return 分子
	 */
	public int getNumerator() {
		return numerator;
	}

	/**
	 * 
	 * @return 分母
	 */
	public int getDenominator() {
		return denominator;
	}

	/**
	 * 约为最简分数。
	 * @return 约分后的对象本身
	 */
	public Fraction reduce() {
		if (!reduced) {
			int d = MathUtil.greatestCommonDivisor(numerator, denominator);
			if (d > 1) {
				numerator /= d;
				denominator /= d;
			}
			reduced = true;
		}
		return this;
	}

	/**
	 * format: "numerator/denominator"
	 */
	@Override
	public String toString() {
		return String.format("%s/%s", numerator, denominator);
	}
}
