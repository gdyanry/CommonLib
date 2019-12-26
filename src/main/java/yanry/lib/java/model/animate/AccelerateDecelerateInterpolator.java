package yanry.lib.java.model.animate;

/**
 * 基于三角函数的动画插值器，可实现【慢-快-慢】的动画效果。
 * <p>
 * Created by yanry on 2019/12/21.
 */
public class AccelerateDecelerateInterpolator implements TimeInterpolator {
    @Override
    public long getInterpolation(long elapsedTime, long period) {
        long sum = elapsedTime + period;
        return (long) (period * ((Math.cos(Math.PI * sum / period) / 2.0) + 0.5));
    }
}
