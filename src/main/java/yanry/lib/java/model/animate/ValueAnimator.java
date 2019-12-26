package yanry.lib.java.model.animate;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.MathUtil;

/**
 * 值动画辅助类，用于计算给定配置下任意时间点的动画值。
 * 该类极致简洁，不提供监听回调，也不保存任何动画值，可以在任一时刻计算多组动画值，满足大多数使用场景的同时保持高性能；
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
    private long startTime;
    private long pauseTime;
    private long period;
    private int repeatCount;

    /**
     * @param period 动画周期。
     */
    public ValueAnimator(long period) {
        super(false);
        this.period = period;
        startTime = System.currentTimeMillis();
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
     * 获取当前动画时间。
     *
     * @return
     */
    public long getElapsedTime() {
        return pauseTime > 0 ? pauseTime - startTime : System.currentTimeMillis() - startTime;
    }

    /**
     * 获取当前动画的进度。
     *
     * @return 当前时间进度与动画周期的比值。
     */
    public float getProgressRatio() {
        if (period > 0) {
            return 1f * getElapsedTime() / period;
        }
        return Float.NaN;
    }

    /**
     * 查询当前动画是否处于暂停的状态。
     *
     * @return
     */
    public boolean isPause() {
        return pauseTime > 0;
    }

    /**
     * 控制动画的暂停和播放。
     *
     * @param pause
     */
    public void setPause(boolean pause) {
        if (pause) {
            pauseTime = System.currentTimeMillis();
        } else if (pauseTime > 0) {
            startTime += System.currentTimeMillis() - pauseTime;
            pauseTime = 0;
        }
    }

    /**
     * 查询当前动画是否已结束。结束意味着在不改变配置的前提下，动画值将永远不会改变。
     *
     * @return
     */
    public boolean isFinish() {
        if (period <= 0) {
            return true;
        }
        return repeatCount > 0 && getElapsedTime() >= period * repeatCount;
    }

    /**
     * 手动调节动画进度。
     *
     * @param ratio 目标进度时间和动画周期的比值，不能小于0。
     */
    public void seekTo(float ratio) {
        if (period >= 0) {
            long now = System.currentTimeMillis();
            // 这里要尽早把float转成long，再和long进行运算，尽可能避免long自动转成float（损失精度）的情况，导致结果错误。
            startTime = now - (long) (period * ratio);
            if (pauseTime > 0) {
                pauseTime = now;
            }
        }
    }

    /**
     * 获取当前时间的动画值。
     *
     * @param controlValues 用于控制（计算）动画值的关键值。
     * @param interpolator
     * @return
     */
    public float getAnimatedValue(float[] controlValues, TimeInterpolator interpolator) {
        if (controlValues == null || controlValues.length == 0) {
            throw new IllegalArgumentException("invalid key values: " + controlValues);
        }
        int maxIndex = controlValues.length - 1;
        float endValue = controlValues[maxIndex];
        if (period <= 0) {
            return endValue;
        }
        long elapsedTime = getElapsedTime();
        float startValue = controlValues[0];
        if (repeatCount > 0 && elapsedTime >= period * repeatCount) {
            return !hasFlag(FLAG_FILL_END) || hasFlag(FLAG_REVERSE) && MathUtil.isEven(repeatCount) ? startValue : endValue;
        }
        boolean isForward = !hasFlag(FLAG_REVERSE) || (elapsedTime / period & 1) == 0;
        long validElapsedTime = elapsedTime % period;
        if (interpolator != null) {
            long copy = validElapsedTime;
            validElapsedTime = interpolator.getInterpolation(copy, period);
            if (validElapsedTime < 0) {
                Logger.getDefault().w("invalid interpolated value %s for argument (%s, %s)", validElapsedTime, copy, period);
                validElapsedTime = 0;
            } else if (validElapsedTime > period) {
                Logger.getDefault().w("invalid interpolated value %s for argument (%s, %s)", validElapsedTime, copy, period);
                validElapsedTime = period;
            }
            if (validElapsedTime == period) {
                validElapsedTime = 0;
                isForward = !isForward;
            }
        }
        if (validElapsedTime == 0) {
            return isForward ? startValue : endValue;
        }
        int fromIndex = (int) (validElapsedTime * maxIndex / period);
        long sectionLen = period / maxIndex;
        long extra = validElapsedTime % sectionLen;
        if (!isForward) {
            fromIndex = controlValues.length - 2 - fromIndex;
            extra = sectionLen - extra;
        }
        startValue = controlValues[fromIndex];
        endValue = controlValues[fromIndex + 1];
        return startValue + (endValue - startValue) * (extra * maxIndex) / period;
    }

    /**
     * 获取当前时间的动画值。
     *
     * @param controlValues 用于控制（计算）动画值的关键值。
     * @param interpolator
     * @return
     */
    public int getAnimatedValue(int[] controlValues, TimeInterpolator interpolator) {
        if (controlValues == null || controlValues.length == 0) {
            throw new IllegalArgumentException("invalid key values: " + controlValues);
        }
        int maxIndex = controlValues.length - 1;
        int endValue = controlValues[maxIndex];
        if (period <= 0) {
            return endValue;
        }
        long elapsedTime = getElapsedTime();
        int startValue = controlValues[0];
        if (repeatCount > 0 && elapsedTime >= period * repeatCount) {
            return !hasFlag(FLAG_FILL_END) || hasFlag(FLAG_REVERSE) && MathUtil.isEven(repeatCount) ? startValue : endValue;
        }
        long validElapsedTime = elapsedTime % period;
        boolean isForward = !hasFlag(FLAG_REVERSE) || ((elapsedTime / period) & 1) == 0;
        if (interpolator != null) {
            long copy = validElapsedTime;
            validElapsedTime = interpolator.getInterpolation(copy, period);
            if (validElapsedTime < 0) {
                Logger.getDefault().w("invalid interpolated value %s for argument (%s, %s)", validElapsedTime, copy, period);
                validElapsedTime = 0;
            } else if (validElapsedTime > period) {
                Logger.getDefault().w("invalid interpolated value %s for argument (%s, %s)", validElapsedTime, copy, period);
                validElapsedTime = period;
            }
            if (validElapsedTime == period) {
                validElapsedTime = 0;
                isForward = !isForward;
            }
        }
        if (validElapsedTime == 0) {
            return isForward ? startValue : endValue;
        }
        int fromIndex = (int) (validElapsedTime * maxIndex / period);
        long sectionLen = period / maxIndex;
        long extra = validElapsedTime % sectionLen;
        if (!isForward) {
            fromIndex = controlValues.length - 2 - fromIndex;
            extra = sectionLen - extra;
        }
        startValue = controlValues[fromIndex];
        endValue = controlValues[fromIndex + 1];
        return (int) (startValue + (endValue - startValue) * extra * maxIndex / period);
    }
}
