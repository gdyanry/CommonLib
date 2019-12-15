package yanry.lib.java.model.schedule;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.model.log.Logger;

import java.util.HashSet;
import java.util.LinkedList;

public class ShowData extends FlagsHolder implements Runnable {
    public static final int FLAG_REJECT_EXPELLED = 1;
    public static final int FLAG_REJECT_DISMISSED = 2;
    public static final int FLAG_EXPEL_WAITING_DATA = 4;
    public static final int FLAG_INVALID_ON_DEQUEUE = 8;

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
    private int state;
    private LinkedList<OnDataStateChangeListener> stateListeners;

    public ShowData() {
        super(false);
        stateListeners = new LinkedList<>();
    }

    public int getState() {
        return state;
    }

    public void dismiss(long delay) {
        if (scheduler != null && state == STATE_SHOWING) {
            if (delay > 0) {
                scheduler.manager.runner.scheduleTimeout(this, delay);
            } else {
                scheduler.manager.runner.cancelPendingTimeout(this);
                scheduler.manager.runner.run(() -> {
                    Logger.getDefault().vv("dismiss by manual: ", this);
                    doDismiss();
                });
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
        Logger.getDefault().vv("dismiss by timeout: ", this);
        doDismiss();
    }

    private void doDismiss() {
        if (scheduler != null && state == STATE_SHOWING) {
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
