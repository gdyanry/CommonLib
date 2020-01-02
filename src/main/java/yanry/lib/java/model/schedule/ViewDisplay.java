package yanry.lib.java.model.schedule;

import java.util.LinkedList;
import java.util.List;

import yanry.lib.java.interfaces.OnValueChangeListener;

/**
 * Created by yanry on 2020/1/2.
 *
 * @param <D> data type.
 * @param <V> view type.
 */
public abstract class ViewDisplay<D extends ShowData, V> extends Display<D> {
    private V view;
    private List<OnValueChangeListener<V>> onViewChangeListeners;
    private String name;

    public ViewDisplay() {
        onViewChangeListeners = new LinkedList<>();
        name = getClass().getSimpleName();
    }

    public V getView() {
        return view;
    }

    protected void setView(V view) {
        if (this.view != view) {
            if (scheduler.manager.logger != null) {
                scheduler.manager.logger.vv(name, " set view: ", view);
            }
            for (OnValueChangeListener<V> listener : onViewChangeListeners) {
                listener.onValueChange(view, this.view);
            }
            this.view = view;
        }
    }

    public void addOnViewChangeListener(OnValueChangeListener<V> listener) {
        if (!onViewChangeListeners.contains(listener)) {
            onViewChangeListeners.add(listener);
        }
    }

    public void removeOnViewChangeListener(OnValueChangeListener<V> listener) {
        onViewChangeListeners.remove(listener);
    }

    /**
     * 此数据界面被提前关闭（非超时，比如由用户按返回键触发）时需要调用此方法通知显示队列中等待的数据，否则队列中下一条数据要等到前一条数据超时时间后才会显示。
     */
    public boolean notifyDismiss(V view) {
        if (view == this.view && scheduler != null && scheduler.current != null && scheduler.current.display == this) {
            ShowData currentData = scheduler.current;
            scheduler.current = null;
            new ScheduleRunnable(scheduler.manager) {
                @Override
                protected void doRun() {
                    setView(null);
                    scheduler.manager.runner.cancelPendingTimeout(currentData);
                    if (scheduler.manager.logger != null) {
                        scheduler.manager.logger.vv("notify dismiss: ", currentData);
                    }
                    currentData.dispatchState(ShowData.STATE_DISMISS);
                    scheduler.manager.rebalance(null, null);
                }
            }.start(name, " notify dismiss: ", view);
            return true;
        }
        return false;
    }

    public final boolean isShowing() {
        if (view != null) {
            return isShowing(view);
        }
        return false;
    }

    protected abstract void dismiss(V view);

    protected abstract boolean isShowing(V view);

    @Override
    protected void internalDismiss() {
        if (view != null) {
            dismiss(view);
            setView(null);
        }
    }
}
