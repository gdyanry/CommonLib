package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.Logger;

import java.util.HashSet;
import java.util.LinkedList;

public class ShowData implements Runnable {
    public static final int FLAG_REJECT_EXPELLED = 1;
    public static final int FLAG_REJECT_DISMISSED = 2;
    public static final int FLAG_EXPEL_WAITING_DATA = 4;
    public static final int FLAG_INVALID_ON_DEQUEUE = 8;

    public static final int STRATEGY_APPEND_TAIL = 0;
    public static final int STRATEGY_INSERT_HEAD = 1;
    public static final int STRATEGY_SHOW_IMMEDIATELY = 2;

    /**
     * dismissed by {@link #dismiss(long)} or {@link Display#dismiss(long)} with delay <= 0.
     */
    public static final String DISMISS_MANUAL = "DISMISS_MANUAL";
    /**
     * dismissed by {@link #dismiss(long)} or {@link Display#dismiss(long)} with delay > 0,
     * or {@link #setDuration(long)} with duration > 0 has been set before {@link Scheduler#show(ShowData, Class)}.
     */
    public static final String DISMISS_TIMEOUT = "DISMISS_TIMEOUT";
    /**
     * dismissed when subsequent data's {@link #strategy} = {@link #STRATEGY_SHOW_IMMEDIATELY} and (has higher priority or (has equal priority and
     * current data's {@link #FLAG_REJECT_DISMISSED} flag is miss)).
     */
    public static final String DISMISS_EXPELLED = "DISMISS_EXPELLED";
    /**
     * dismissed by {@link Scheduler#cancel(boolean)} or {@link SchedulerManager#cancelAll(boolean)} or {@link SchedulerManager#cancelByTag(Object)}.
     */
    public static final String DISMISS_CANCELLED = "DISMISS_CANCELLED";
    /**
     * dismissed by {@link Display#notifyDismiss(Object)}.
     */
    public static final String DISMISS_NOTIFIED = "DISMISS_NOTIFIED";
    /**
     * dequeue when subsequent data' has equal or higher priority and {@link #FLAG_EXPEL_WAITING_DATA} flag and current data's
     * {@link #FLAG_REJECT_EXPELLED} flag is miss.
     */
    public static final String DEQUEUE_EXPELLED = "DEQUEUE_EXPELLED";
    /**
     * dequeue by {@link Scheduler#cancel(boolean)} or {@link SchedulerManager#cancelAll(boolean)} or {@link SchedulerManager#cancelByTag(Object)}.
     */
    public static final String DEQUEUE_CANCELLED = "DEQUEUE_CANCELLED";
    /**
     * dequeue if current data has {@link #FLAG_INVALID_ON_DEQUEUE} flag when rebalancing the scheduler queue.
     */
    public static final String DEQUEUE_INVALID = "DEQUEUE_INVALID";

    Object extra;
    long duration;
    Scheduler scheduler;
    Object tag;
    Display display;
    int priority;
    int strategy;
    int flags;
    private LinkedList<Runnable> onShowListeners;
    private LinkedList<OnReleaseListener> onReleaseListeners;

    public ShowData() {
        strategy = STRATEGY_SHOW_IMMEDIATELY;
        onShowListeners = new LinkedList<>();
        onReleaseListeners = new LinkedList<>();
    }

    public boolean isScheduled() {
        return display != null;
    }

    public boolean isShowing() {
        return scheduler != null && scheduler.current == this;
    }

    public void dismiss(long delay) {
        if (scheduler != null && scheduler.current == this) {
            if (delay > 0) {
                scheduler.manager.runner.scheduleTimeout(this, delay);
            } else {
                scheduler.manager.runner.cancelPendingTimeout(this);
                scheduler.manager.runner.run(() -> doDismiss(DISMISS_MANUAL));
            }
        }
    }

    public ShowData setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public ShowData setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public ShowData setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @param strategy must be one of {@link #STRATEGY_SHOW_IMMEDIATELY}, {@link #STRATEGY_INSERT_HEAD}, {@link #STRATEGY_APPEND_TAIL}.
     */
    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public int getFlags() {
        return flags;
    }

    /**
     * @param flags available values are {@link #FLAG_REJECT_EXPELLED}, {@link #FLAG_REJECT_DISMISSED},
     *              {@link #FLAG_EXPEL_WAITING_DATA}, {@link #FLAG_INVALID_ON_DEQUEUE}.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Object getExtra() {
        return extra;
    }

    public ShowData setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    /**
     * on show means exactly after shown.
     *
     * @param listener
     * @return
     */
    public ShowData addOnShowListener(Runnable listener) {
        onShowListeners.add(listener);
        return this;
    }

    /**
     * on release generally means before dequeue or dismiss, except when release type is {@link #DISMISS_NOTIFIED},
     * which is determined by the timing of invoking {@link Display#notifyDismiss(Object)}.
     *
     * @param listener
     * @return
     */
    public ShowData addOnReleaseListener(OnReleaseListener listener) {
        onReleaseListeners.add(listener);
        return this;
    }

    final void dispatchShow() {
        for (Runnable listener : onShowListeners) {
            listener.run();
        }
    }

    final void dispatchRelease(String type) {
        Logger.getDefault().v("release(%s): %s", type, this);
        for (OnReleaseListener listener : onReleaseListeners) {
            listener.onRelease(type);
        }
    }

    protected boolean rejectExpelled() {
        return (flags & FLAG_REJECT_EXPELLED) == FLAG_REJECT_EXPELLED;
    }

    protected boolean rejectDismissed() {
        return (flags & FLAG_REJECT_DISMISSED) == FLAG_REJECT_DISMISSED;
    }

    protected boolean expelWaitingData(ShowData data) {
        return (flags & FLAG_EXPEL_WAITING_DATA) == FLAG_EXPEL_WAITING_DATA;
    }

    /**
     * 当数据从队列中取出显示时回调此方法，如果返回false则不显示直接丢弃
     *
     * @return
     */
    protected boolean isValidOnDequeue() {
        return (flags & FLAG_INVALID_ON_DEQUEUE) == 0;
    }

    @Override
    public final void run() {
        doDismiss(DISMISS_TIMEOUT);
    }

    private void doDismiss(String type) {
        if (scheduler != null && scheduler.current == this) {
            scheduler.current = null;
            dispatchRelease(type);
            HashSet<Display> displaysToDismisses = new HashSet<>();
            displaysToDismisses.add(display);
            scheduler.manager.rebalance(null, displaysToDismisses);
        }
    }

    @Override
    public String toString() {
        return extra == null ? super.toString() : extra.toString();
    }
}
