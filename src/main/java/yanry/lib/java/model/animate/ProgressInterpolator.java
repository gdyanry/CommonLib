package yanry.lib.java.model.animate;

/**
 * 进度比例转换接口。
 */
public interface ProgressInterpolator {
    /**
     * 基于三角函数插值，可实现【慢-快-慢】的动画效果。
     *
     * @param input
     * @return
     */
    static float accelerateDecelerateInterpolate(float input) {
        return (float) (Math.cos(Math.PI * (1 + input)) / 2 + 0.5);
    }

    /**
     * 基于指数函数插值，可以通过设置指数值实现加速/减速效果。
     *
     * @param input
     * @param exponent 指数值，取值范围[0, ++)。当小于1时为对数函数，即先快后慢；大于1时为指数函数，即先慢后快；等于1时为线性函数，相当于不起作用。
     * @return
     */
    static float powerInterpolate(float input, float exponent) {
        if (exponent == 0) {
            return 1;
        }
        if (exponent == 1) {
            return input;
        }
        if (exponent == 2) {
            return input * input;
        }
        return (float) Math.pow(input, exponent);
    }

    /**
     * 将原来密度均匀的数值等分映射到给定区间中，导致各区间之间数值密度不相等。
     *
     * @param input
     * @param sections 区间分界点值，必须在0到1之间且按升序排列。
     * @return
     */
    static float sectionInterpolate(float input, float... sections) {
        if (sections.length == 0) {
            throw new IllegalArgumentException("number of section must be > 0");
        }
        float previous = 0;
        for (float section : sections) {
            if (section < previous || section > 1) {
                throw new IllegalArgumentException("section values must be lined in ascending order and <= 1");
            }
        }
        float lastSection = 0;
        int length = sections.length;
        for (int i = 0; i < length; i++) {
            float section = sections[i];
            if (input > section) {
                lastSection = section;
            } else if (input == section) {
                return 1f * (i + 1) / (length + 1);
            } else {
                return ValueAnimator.calculateValueByProportion(1f * i / (length + 1), 1f * (i + 1) / (length + 1), (input - lastSection) / (section - lastSection));
            }
        }
        return ValueAnimator.calculateValueByProportion(1f * length / (length + 1), 1, (input - lastSection) / (1 - lastSection));
    }

    /**
     * @param input 输入值，取值范围(0, 1)
     * @return 变换值，取值范围为任意浮点数
     */
    float getInterpolation(float input);
}
