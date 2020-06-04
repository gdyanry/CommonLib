package yanry.lib.java.model.animate;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.util.MathUtil;

/**
 * 值动画辅助类，用于计算给定配置下任意时间点的动画值。
 * 该类极致简洁，不提供监听回调，也不保存任何动画值，仅仅封装了动画的配置，结合TimeController使用可满足大多数使用场景的同时保持高性能；
 * 同时不存在使用结束后因为忘记关闭而导致资源泄露的情况，实际上也没有close/release/cancel之类的函数。
 *
 * <p>
 * Created by yanry on 2019/12/19.
 */
public class ValueAnimator extends FlagsHolder {
    /**
     * 动画循环时反向播放。默认重新正向播放。
     */
    public static final int FLAG_REVERSE = 1;
    /**
     * 动画结束时维持结束时的状态。默认会恢复起始状态。
     */
    public static final int FLAG_FILL_END = 2;

    public static float calculateValueByProportion(float from, float to, float proportion) {
        if (proportion == 0) {
            return from;
        }
        if (proportion == 1) {
            return to;
        }
        return from + (to - from) * proportion;
    }

    private long period;
    private int repeatCount;

    /**
     * @param period 动画周期。
     */
    public ValueAnimator(long period) {
        super(false);
        if (period <= 0) {
            throw new IllegalArgumentException("period must be > 0");
        }
        this.period = period;
    }

    /**
     * 设置动画循环次数。
     *
     * @param repeatCount 动画循环次数，当小于或等于0时表示无限循环。
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /**
     * 查询当前动画在给定时间下是否已结束。结束意味着在不改变配置的前提下，动画值将不再改变。
     *
     * @param elapsedTime
     * @return
     */
    public boolean isFinish(long elapsedTime) {
        if (period <= 0) {
            return true;
        }
        return repeatCount > 0 && elapsedTime >= period * repeatCount;
    }

    public long getPeriod() {
        return period;
    }

    /**
     * 获取给定时间的动画值。
     *
     * @param elapsedTime  时间
     * @param interpolator 进度篡改器
     * @param keyValues    关键值
     * @return
     */
    public float getAnimatedValue(long elapsedTime, ProgressInterpolator interpolator, float... keyValues) {
        if (keyValues.length < 2) {
            throw new IllegalArgumentException("number of key value must be >= 2");
        }
        if (elapsedTime <= 0) {
            return keyValues[0];
        } else if (repeatCount > 0 && elapsedTime >= period * repeatCount) {
            return !hasFlag(FLAG_FILL_END) || hasFlag(FLAG_REVERSE) && MathUtil.isEven(repeatCount) ? keyValues[0] : keyValues[keyValues.length - 1];
        }
        boolean isForward = !hasFlag(FLAG_REVERSE) || (elapsedTime / period & 1) == 0;
        elapsedTime = elapsedTime % period;
        if (elapsedTime == 0) {
            return isForward ? keyValues[0] : keyValues[keyValues.length - 1];
        }
        float proportion = 1f * elapsedTime / period;
        if (!isForward) {
            proportion = 1 - proportion;
        }
        if (interpolator != null) {
            proportion = interpolator.getInterpolation(proportion);
        }
        float sectionUnit = 1f / (keyValues.length - 1);
        if (proportion < 0) {
            return calculateValueByProportion(keyValues[0], keyValues[1], proportion / sectionUnit);
        }
        if (proportion > 1) {
            return calculateValueByProportion(keyValues[keyValues.length - 2], keyValues[keyValues.length - 1], (proportion - 1) / sectionUnit);
        }
        int startIndex = 0;
        float lastSection = 0;
        for (int i = 0; i < keyValues.length; i++) {
            float section = sectionUnit * i;
            if (proportion > section) {
                startIndex = i;
                lastSection = section;
            } else if (proportion == section) {
                return keyValues[i];
            } else {
                break;
            }
        }
        return calculateValueByProportion(keyValues[startIndex], keyValues[startIndex + 1], (proportion - lastSection) / sectionUnit);
    }
}
