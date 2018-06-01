package lib.common.model;

/**
 * 该类代表某个连续重复的动作，例如连接点击某个按钮。
 * @author rongyu.yan
 * @date 2018/3/22.
 */

public abstract class RepeatAction {
    private long maxInterval;
    private int count;
    private long lastTimestamp;

    /**
     *
     * @param maxInterval 重复动作之间的最大时间间隔。如果先后两个动作的间隔超出此时间，则计数重置为0。
     */
    public RepeatAction(long maxInterval) {
        this.maxInterval = maxInterval;
    }

    public void tick() {
        long temp = lastTimestamp;
        lastTimestamp = System.currentTimeMillis();
        if (lastTimestamp - temp > maxInterval) {
            count = 0;
        }
        consumeAction(++count);
    }

    /**
     *
     * @param count 动作触发的次数。
     */
    protected abstract void consumeAction(int count);
}
