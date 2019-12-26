package yanry.lib.java.model.animate;

/**
 * 基于指数函数的时间插值器，可以通过设置指数值实现加速/减速效果。
 * <p>
 * Created by yanry on 2019/12/21.
 */
public class PowerInterpolator implements TimeInterpolator {
    private float exponent;

    /**
     * @param exponent 指数值，取值范围[0, ++)。当小于1时为对数函数，即先快后慢；大于1时为指数函数，即先慢后快；等于1时为线性函数，相当于不起作用。
     */
    public PowerInterpolator(float exponent) {
        this.exponent = exponent;
    }

    @Override
    public long getInterpolation(long elapsedTime, long period) {
        if (exponent == 0) {
            return period;
        }
        if (exponent == 1f) {
            return elapsedTime;
        }
        if (exponent == 2f && elapsedTime <= 3037000499L) {
            return elapsedTime * elapsedTime / period;
        }
        if (exponent == 0.5f && period <= 3037000499L) {
            return (long) Math.sqrt(elapsedTime * period);
        }
        return (long) (period * Math.pow(1d * elapsedTime / period, exponent));
    }
}
