package yanry.lib.java.model.schedule;

import yanry.lib.java.interfaces.OnValueChangeListener;

import java.util.LinkedList;
import java.util.List;

/**
 * 为特定数据显示特定界面。非抽象子孙类必须包含无参构造函数。
 *
 * @param <D> data type.
 * @param <V> view type.
 */
public abstract class Display<D extends ShowData, V> {
    private Scheduler scheduler;
    private V view;
    private List<OnValueChangeListener<V>> onPopInstanceChangeListeners;

    protected Display() {
        onPopInstanceChangeListeners = new LinkedList<>();
    }

    void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected V getView() {
        return view;
    }

    protected void setView(V view) {
        if (this.view != view) {
            for (OnValueChangeListener<V> listener : onPopInstanceChangeListeners) {
                listener.onValueChange(view, this.view);
            }
            this.view = view;
        }
    }

    public void addOnPopInstanceChangeListener(OnValueChangeListener<V> listener) {
        if (!onPopInstanceChangeListeners.contains(listener)) {
            onPopInstanceChangeListeners.add(listener);
        }
    }

    public void removeOnPopInstanceChangeListener(OnValueChangeListener<V> listener) {
        onPopInstanceChangeListeners.remove(listener);
    }

    public D getProcessingData() {
        if (scheduler != null && scheduler.current != null && scheduler.current.display == this) {
            return (D) scheduler.current;
        }
        return null;
    }

    /**
     * 此数据界面被提前关闭（非超时，比如由用户按返回键触发）时需要调用此方法通知显示队列中等待的数据，否则队列中下一条数据要等到前一条数据超时时间后才会显示。
     */
    public boolean notifyDismiss(V view) {
        if (view == this.view && scheduler != null && scheduler.current != null && scheduler.current.display == this) {
            ShowData currentTask = scheduler.current;
            scheduler.current = null;
            scheduler.manager.runner.run(() -> {
                setView(null);
                scheduler.manager.runner.cancelPendingTimeout(currentTask);
                currentTask.dispatchDismiss(ShowData.DISMISS_TYPE_NOTIFIED);
                scheduler.manager.rebalance(null, null);
            });
            return true;
        }
        return false;
    }

    public void dismiss(long delay) {
        if (scheduler != null && scheduler.current != null && scheduler.current.display == this) {
            scheduler.current.dismiss(delay);
        }
    }

    protected void internalDismiss() {
        if (view != null) {
            dismiss(view);
            setView(null);
        }
    }

    public final boolean isShowing() {
        if (view != null) {
            return isShowing(view);
        }
        return false;
    }

    protected abstract void show(D data);

    protected abstract void dismiss(V view);

    protected abstract boolean isShowing(V view);
}
