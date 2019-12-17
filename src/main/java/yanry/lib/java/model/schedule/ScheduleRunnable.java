package yanry.lib.java.model.schedule;

/**
 * Created by yanry on 2019/12/17.
 */
public abstract class ScheduleRunnable implements Runnable {
    private SchedulerManager manager;

    public ScheduleRunnable(SchedulerManager manager) {
        this.manager = manager;
    }

    public void start() {
        manager.runner.run(this);
    }

    @Override
    public void run() {
        if (manager.isRunning) {
            manager.runner.scheduleTimeout(this, 0);
        } else {
            manager.isRunning = true;
            doRun();
            manager.isRunning = false;
        }
    }

    protected abstract void doRun();
}
