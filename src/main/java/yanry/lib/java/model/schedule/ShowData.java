package yanry.lib.java.model.schedule;

import java.util.HashSet;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.model.watch.ValueHolder;
import yanry.lib.java.model.watch.ValueHolderImpl;

/**
 * 需要“显示”的数据
 */
public class ShowData extends FlagsHolder implements Runnable {
    /**
     * 拒绝被清出队列
     */
    public static final int FLAG_REJECT_EXPELLED = 1;
    /**
     * 拒绝被关闭
     */
    public static final int FLAG_REJECT_DISMISSED = 2;
    /**
     * 清理队列
     */
    public static final int FLAG_EXPEL_WAITING_DATA = 4;
    /**
     * 进入队列后不再显示
     */
    public static final int FLAG_INVALID_ON_DELAYED_SHOW = 8;

    /**
     * 如果有数据正在显示则放入队列尾部等候
     */
    public static final int STRATEGY_APPEND_TAIL = 2;
    /**
     * 如果有数据正在显示则放入队列首部等候
     */
    public static final int STRATEGY_INSERT_HEAD = 1;
    /**
     * 如果有数据正在显示则将其关闭，立即显示
     */
    public static final int STRATEGY_SHOW_IMMEDIATELY = 0;

    /**
     * 正在队列中等候
     */
    public static final int STATE_ENQUEUE = 1;
    /**
     * 从队列中被清理
     */
    public static final int STATE_DEQUEUE = 2;
    /**
     * 正在显示中
     */
    public static final int STATE_SHOWING = 3;
    /**
     * 已关闭
     */
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
        // 此处scheduler.showingData==this不能用state==STATE_SHOWING替代，因为state是在show完成之后才更新，而实际使用有可能会在show的过程中调用dismiss。
        if (scheduler != null && scheduler.showingData.getValue() == this) {
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
        if (scheduler != null && scheduler.showingData.getValue() == this) {
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
        if (scheduler != null) {
            if (scheduler.showingData.getValue() == this) {
                scheduler.showingData.setValue(null);
                setState(STATE_DISMISS);
                HashSet<Display> displaysToDismisses = new HashSet<>();
                displaysToDismisses.add(display);
                scheduler.manager.rebalance(null, displaysToDismisses);
            } else {
                setState(STATE_DEQUEUE);
                scheduler.manager.queue.remove(this);
            }
        }
    }

    @Override
    public String toString() {
        return extra == null ? super.toString() : extra.toString();
    }
}
