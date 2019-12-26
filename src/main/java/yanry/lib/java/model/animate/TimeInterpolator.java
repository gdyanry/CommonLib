package yanry.lib.java.model.animate;

/**
 * 动画时间插值接口，用于实现加减速等时间相关的动画效果。
 * <p>
 * Created by yanry on 2019/12/21.
 */
public interface TimeInterpolator {
    /**
     * @param elapsedTime 实际动画流逝时间。
     * @param period      动画周期。
     * @return 篡改后的动画流逝时间，取值范围[0, period)。
     */
    long getInterpolation(long elapsedTime, long period);
}
