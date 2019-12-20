package yanry.lib.java.util;

/**
 * @author yanry
 * <p>
 * 2015年5月14日 上午9:38:36
 */
public class MathUtil {

    /**
     * 偶数判断
     *
     * @param value
     * @return
     */
    public static boolean isEven(int value) {
        return (value & 1) == 0;
    }

    /**
     * 最大公约数
     *
     * @param value1
     * @param value2
     * @return
     */
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

    /**
     * 贝塞尔曲线公式
     *
     * @param fraction     the proportion between the start and end values.
     * @param startValue
     * @param endValue
     * @param medianValues
     * @return
     */
    public static int bezierEvaluate(float fraction, int startValue, int endValue, int... medianValues) {
        int n = medianValues.length + 1;
        int value = (int) (startValue * Math.pow(1 - fraction, n) + endValue * Math.pow(fraction, n));
        for (int i = 1; i < n; i++) {
            value += getPascalTriangleCoefficient(n, i) * medianValues[i - 1] * Math.pow(1 - fraction, n - i) * Math.pow(fraction, i);
        }
        return value;
    }

    /**
     * 杨辉三角系数
     *
     * @param power
     * @param index
     * @return
     */
    public static int getPascalTriangleCoefficient(int power, int index) {
        if (power >= 0 && index >= 0 && index < power) {
            if (index == 0 || index == power - 1) {
                return 1;
            }
            return getPascalTriangleCoefficient(power - 1, index - 1) + getPascalTriangleCoefficient(power - 1, index);
        }
        throw new IllegalArgumentException(String.format("power:%s, index:%s", power, index));
    }
}
