/**
 * 
 */
package lib.common.util;

/**
 * @author yanry
 *
 *         2015年5月14日 上午9:38:36
 */
public class MathUtil {

	public static boolean isEven(int value) {
		return (value & 0x01) != 1;
	}

	public static int greatestCommonDivisor(int value1, int value2) {
		int greatest_common_divisor = 1;
		if (isEven(value1) && isEven(value2)) {
			if (value1 > 0 && value2 > 0) {
				greatest_common_divisor *= 2;
				greatest_common_divisor *= greatestCommonDivisor(value1 >> 1,
						value2 >> 1);
			} else {
				return 1;
			}
		} else if (isEven(value1)) {
			if (value1 > 0) {
				greatest_common_divisor *= greatestCommonDivisor(value1 >> 1,
						value2);
			} else {
				return 1;
			}
		} else if (isEven(value2)) {
			if (value2 > 0) {
				greatest_common_divisor *= greatestCommonDivisor(value1,
						value2 >> 1);
			} else {
				return 1;
			}
		} else {
			if (value1 > value2) {
				greatest_common_divisor *= greatestCommonDivisor(value1
						- value2, value2);
			} else if (value1 < value2) {
				greatest_common_divisor *= greatestCommonDivisor(value1, value2
						- value1);
			} else {
				greatest_common_divisor *= value1;
			}
		}
		return greatest_common_divisor;
	}
}
