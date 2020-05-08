package yanry.lib.java.model.schedule;

import java.util.HashSet;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.model.watch.ValueHolder;
import yanry.lib.java.model.watch.ValueHolderImpl;

public class ShowData extends FlagsHolder implements Runnable {
    public static final int FLAG_REJECT_EXPELLED = 1;
    public static final int FLAG_REJECT_DISMISSED = 2;
    public static final int FLAG_EXPEL_WAITING_DATA = 4;
    public static final int FLAG_INVALID_ON_DELAYED_SHOW = 8;
    public static final int FLAG_DISMISS_ON_SHOW = 16;

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
    private ValueHolderImpl<Integer> state;

    public ShowData() {
        super(false);
        state = new ValueHolderImpl<>(0);
    }

    public ValueHolder<Integer> getState() {
        return state;
    }

    int setState(int state) {
        Integer previous = this.state.setValue(state);
        if (previous != state) {
            onStateChange(state, previous);
        }
        return previous;
    }

    public void dismiss(long delay) {
        // 此处scheduler.current==this不能用state==STATE_SHOWING替代，因为state是在show完成之后才更新，而实际使用有可能会在show的过程中调用dismiss。
        if (scheduler != null && scheduler.current == this) {
            if (delay > 0) {
                scheduler.manager.runner.schedule(this, delay);
            } else {
                new ScheduleRunnable(scheduler.manager) {
                    @Override
                    protected void doRun() {
                        scheduler.manager.runner.cancel(ShowData.this);
                        doDismiss();
                    }
                }.start("dismiss by manual: ", ShowData.this);
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
    public ShowData setStrategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public Object getExtra() {
        return extra;
    }

    public ShowData setExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    public void cancelDismiss() {
        if (scheduler != null && scheduler.current == this) {
            scheduler.manager.runner.cancel(this);
        }
    }

    protected boolean expelWaitingData(ShowData data) {
        return hasFlag(FLAG_EXPEL_WAITING_DATA);
    }

    protected void onStateChange(int to, int from) {
    }

    @Override
    public final void run() {
        new ScheduleRunnable(scheduler.manager) {
            @Override
            protected void doRun() {
                scheduler.manager.runner.cancel(ShowData.this);
                doDismiss();
            }
        }.start("dismiss by timeout: ", ShowData.this);
    }

    private void doDismiss() {
        if (scheduler != null && scheduler.current == this) {
            scheduler.current = null;
            this.state.setValue(STATE_DISMISS);
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
