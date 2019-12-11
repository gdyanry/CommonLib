package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.Logger;

import java.util.HashSet;
import java.util.LinkedList;

public class ShowData implements Runnable {
    public static final int STRATEGY_APPEND_TAIL = 0;
    public static final int STRATEGY_INSERT_HEAD = 1;
    public static final int STRATEGY_SHOW_IMMEDIATELY = 2;

    public static String DISMISS_TYPE_MANUAL = "MANUAL";
    public static String DISMISS_TYPE_TIMEOUT = "TIMEOUT";
    public static String DISMISS_TYPE_EXPELLED = "EXPELLED";
    public static String DISMISS_TYPE_CANCELLED = "CANCELLED";
    public static String DISMISS_TYPE_NOTIFIED = "NOTIFIED";

    Object extra;
    long duration;
    Scheduler scheduler;
    Object tag;
    Display display;
    int priority;
    private LinkedList<Runnable> onShowListeners;
    private LinkedList<OnDismissListener> onDismissListeners;

    public ShowData() {
        onShowListeners = new LinkedList<>();
        onDismissListeners = new LinkedList<>();
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
                scheduler.manager.runner.run(() -> doDismiss(DISMISS_TYPE_MANUAL));
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

    public Object getExtra() {
        return extra;
    }

    public ShowData setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    /**
     * Add callback after this task has been shown.
     */
    public ShowData addOnShowListener(Runnable listener) {
        onShowListeners.add(listener);
        return this;
    }

    public ShowData addOnDismissListener(OnDismissListener listener) {
        onDismissListeners.add(listener);
        return this;
    }

    final void dispatchShow() {
        for (Runnable listener : onShowListeners) {
            listener.run();
        }
    }

    final void dispatchDismiss(String type) {
        Logger.getDefault().v("dismiss(%s): %s", type, this);
        for (OnDismissListener listener : onDismissListeners) {
            listener.onDismiss(type);
        }
    }

    protected int getStrategy() {
        return STRATEGY_SHOW_IMMEDIATELY;
    }

    protected boolean rejectExpelled() {
        return false;
    }

    protected boolean rejectDismissed() {
        return false;
    }

    protected boolean expelWaitingTask(ShowData request) {
        return false;
    }

    /**
     * 当数据从队列中取出显示时回调此方法，如果返回false则不显示直接丢弃
     *
     * @return
     */
    protected boolean isValidOnDequeue() {
        return true;
    }

    protected void onCleanFromQueue() {
    }

    @Override
    public final void run() {
        doDismiss(DISMISS_TYPE_TIMEOUT);
    }

    private void doDismiss(String type) {
        if (scheduler != null && scheduler.current == this) {
            scheduler.current = null;
            dispatchDismiss(type);
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
