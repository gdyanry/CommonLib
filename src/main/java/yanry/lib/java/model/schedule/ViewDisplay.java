package yanry.lib.java.model.schedule;

import yanry.lib.java.model.watch.ValueHolder;
import yanry.lib.java.model.watch.ValueHolderImpl;

/**
 * Created by yanry on 2020/1/2.
 *
 * @param <D> data type.
 * @param <V> view type.
 */
public abstract class ViewDisplay<D extends ShowData, V> extends Display<D> {
    private ValueHolderImpl<V> viewHolder;

    public ViewDisplay() {
        viewHolder = new ValueHolderImpl<>();
    }

    protected void setView(V view) {
        viewHolder.setValue(view);
    }

    public ValueHolder<V> getViewHolder() {
        return viewHolder;
    }

    /**
     * 此数据界面被提前关闭（非超时，比如由用户按返回键触发）时需要调用此方法通知显示队列中等待的数据，否则队列中下一条数据要等到前一条数据超时时间后才会显示。
     */
    public boolean notifyDismiss(V view) {
        if (view == viewHolder.getValue() && scheduler != null && scheduler.current != null && scheduler.current.display == this) {
            ShowData currentData = scheduler.current;
            scheduler.current = null;
            new ScheduleRunnable(scheduler.manager) {
                @Override
                protected void doRun() {
                    setView(null);
                    scheduler.manager.runner.cancel(currentData);
                    if (scheduler.manager.logger != null) {
                        scheduler.manager.logger.vv("notify dismiss: ", currentData);
                    }
                    currentData.state.setValue(ShowData.STATE_DISMISS);
                    scheduler.manager.rebalance(null, null);
                }
            }.start(getClass().getSimpleName(), '@', hashCode(), " notify dismiss: ", view);
            return true;
        }
        return false;
    }

    public final boolean isShowing() {
        V view = viewHolder.getValue();
        if (view != null) {
            return isShowing(view);
        }
        return false;
    }

    protected abstract void dismiss(V view);

    protected abstract boolean isShowing(V view);

    @Override
    protected void internalDismiss() {
        V view = viewHolder.getValue();
        if (view != null) {
            dismiss(view);
            setView(null);
        }
    }
}
