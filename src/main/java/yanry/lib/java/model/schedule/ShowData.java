package yanry.lib.java.model.schedule;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.model.watch.IntHolderImpl;

import java.util.HashSet;

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
    IntHolderImpl stateHolder = new StateHolder();

    public ShowData() {
        super(false);
    }

    public IntHolderImpl getState() {
        return stateHolder;
    }

    public void dismiss(long delay) {
        if (scheduler != null) {
            if (delay > 0) {
                scheduler.manager.runner.schedule(this, delay);
            } else {
                dismiss(false);
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
        if (scheduler != null) {
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
        dismiss(true);
    }

    private void dismiss(boolean isTimeout) {
        new ScheduleRunnable(scheduler.manager) {
            @Override
            protected void doRun() {
                scheduler.manager.runner.cancel(ShowData.this);
                int stateValue = stateHolder.getValue();
                switch (stateValue) {
                    case STATE_SHOWING:
                        scheduler.showingData.setValue(null);
                        stateHolder.setValue(STATE_DISMISS);
                        HashSet<Display> displaysToDismisses = new HashSet<>();
                        displaysToDismisses.add(display);
                        scheduler.manager.rebalance(null, displaysToDismisses);
                        break;
                    case STATE_ENQUEUE:
                        stateHolder.setValue(STATE_DEQUEUE);
                        scheduler.manager.queue.remove(ShowData.this);
                        break;
                    default:
                        if (scheduler.manager.logger != null) {
                            scheduler.manager.logger.ww("quit dismiss for invalid state ", stateValue, ": ", ShowData.this);
                        }
                        break;
                }
            }
        }.start("dismiss by ", isTimeout ? "timeout: " : "manual: ", this);
    }

    @Override
    public String toString() {
        return extra == null ? super.toString() : extra.toString();
    }

    private class StateHolder extends IntHolderImpl {
        @Override
        protected void onDispatchValueChange(int to, int from) {
            if (scheduler.manager.logger != null) {
                scheduler.manager.logger.vv(ShowData.this, " state change: ", from, " -> ", to);
            }
        }
    }
}
