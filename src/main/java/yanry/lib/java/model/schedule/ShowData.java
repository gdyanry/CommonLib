package yanry.lib.java.model.schedule;

import java.util.HashSet;
import java.util.LinkedList;

import yanry.lib.java.model.FlagsHolder;

public class ShowData extends FlagsHolder implements Runnable {
    public static final int FLAG_REJECT_EXPELLED = 1;
    public static final int FLAG_REJECT_DISMISSED = 2;
    public static final int FLAG_EXPEL_WAITING_DATA = 4;
    public static final int FLAG_INVALID_ON_DELAYED_SHOW = 8;

    public static final int STRATEGY_APPEND_TAIL = 2;
    public static final int STRATEGY_INSERT_HEAD = 1;
    public static final int STRATEGY_SHOW_IMMEDIATELY = 0;

    public static final int STATE_ENQUEUE = 1;
    public static final int STATE_DEQUEUE = 2;
    public static final int STATE_SHOWING = 3;
    public static final int STATE_DISMISS = 4;

    Object extra;
    long duration;
    Scheduler scheduler;
    Object tag;
    Display display;
    int priority;
    int strategy;
    int state;
    private LinkedList<OnDataStateChangeListener> stateListeners;

    public ShowData() {
        super(false);
        stateListeners = new LinkedList<>();
    }

    public int getState() {
        return state;
    }

    public void dismiss(long delay) {
        // 此处scheduler.current==this不能用state==STATE_SHOWING替代，因为state是在show完成之后才更新，而实际使用有可能会在show的过程中调用dismiss。
        if (scheduler != null && scheduler.current == this) {
            if (delay > 0) {
                scheduler.manager.runner.scheduleTimeout(this, delay);
            } else {
                new ScheduleRunnable(scheduler.manager) {
                    @Override
                    protected void doRun() {
                        scheduler.manager.runner.cancelPendingTimeout(ShowData.this);
                        if (scheduler.manager.logger != null) {
                            scheduler.manager.logger.vv("dismiss by manual: ", ShowData.this);
                        }
                        doDismiss();
                    }
                }.start(this, " dismiss: ", delay);
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

    public Object getExtra() {
        return extra;
    }

    public ShowData setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    public void addOnStateChangeListener(OnDataStateChangeListener listener) {
        stateListeners.add(listener);
    }

    void dispatchState(int state) {
        for (OnDataStateChangeListener listener : stateListeners) {
            listener.onDataStateChange(state);
        }
        this.state = state;
    }

    protected boolean expelWaitingData(ShowData data) {
        return hasFlag(FLAG_EXPEL_WAITING_DATA);
    }

    @Override
    public final void run() {
        if (scheduler != null) {
            scheduler.manager.isRunning = true;
            if (scheduler.manager.logger != null) {
                scheduler.manager.logger.vv("dismiss by timeout: ", this);
            }
            doDismiss();
            scheduler.manager.isRunning = false;
        }
    }

    private void doDismiss() {
        if (scheduler != null && scheduler.current == this) {
            scheduler.current = null;
            dispatchState(STATE_DISMISS);
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
