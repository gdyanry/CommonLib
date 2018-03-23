package lib.common.model;

/**
 * @author rongyu.yan
 * @date 2018/3/22.
 */

public abstract class ContinuousAction {
    private long maxInterval;
    private int count;
    private long lastTimestamp;

    public ContinuousAction(long maxInterval) {
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

    protected abstract void consumeAction(int count);
}
