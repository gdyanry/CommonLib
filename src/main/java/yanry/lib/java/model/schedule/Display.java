package yanry.lib.java.model.schedule;

/**
 * 为特定数据显示特定界面。非抽象子孙类必须包含无参构造函数。
 *
 * @param <D> data type.
 */
public abstract class Display<D extends ShowData> {
    protected Scheduler scheduler;

    protected Display() {
    }

    void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public D getShowingData() {
        if (scheduler != null && scheduler.showingData.isBoundTo(this)) {
            return (D) scheduler.showingData.getValue();
        }
        return null;
    }

    protected abstract void internalDismiss();

    protected abstract void show(D data);
}
