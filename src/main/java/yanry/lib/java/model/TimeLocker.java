package yanry.lib.java.model;

import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2019/12/14.
 */
public class TimeLocker {
    private long lockTime;
    private long delay;

    /**
     * 锁定，直接调用{@link #unlock(boolean)}或者{@link #lock(long)}后才可能解锁。
     */
    public void lock() {
        setLockTime(-1);
    }

    /**
     * 锁定一段时间后自动解锁。
     *
     * @param millis 锁定时间。
     */
    public void lock(long millis) {
        this.delay = millis;
        setLockTime(System.currentTimeMillis());
    }

    /**
     * 手动解锁。
     *
     * @param force 为true表示马上解锁；为false时，只有未设定自动解锁时间时才执行解锁。
     */
    public void unlock(boolean force) {
        if (force || lockTime < 0) {
            setLockTime(0);
        }
    }

    /**
     * @param lockTime 大于0时（取当前时间戳）表示vui在现在起不少于{@link #getMillisToUnlock()}后才可以更新；
     *                 为0表示可以立即更新;
     *                 为-1时，表示当前不能更新，直到调用unlock()才接受更新。
     */
    private void setLockTime(long lockTime) {
        Logger.getDefault().vv(this, " setLockTime: " + lockTime);
        this.lockTime = lockTime;
    }

    /**
     * 获取现在距离解锁时间的间隔。
     *
     * @return 等于0表示已解锁；小于0表示未解锁；大于0表示该时间后自动解锁。
     */
    public long getMillisToUnlock() {
        if (lockTime > 0) {
            long delta = delay + lockTime - System.currentTimeMillis();
            if (delta > 0) {
                return delta;
            }
            return 0;
        }
        return lockTime;
    }
}
