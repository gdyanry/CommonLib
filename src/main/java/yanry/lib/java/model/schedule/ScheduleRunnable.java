package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.LogLevel;

/**
 * Created by yanry on 2019/12/17.
 */
public abstract class ScheduleRunnable implements Runnable {
    private SchedulerManager manager;

    public ScheduleRunnable(SchedulerManager manager) {
        this.manager = manager;
    }

    public void start(Object... logParts) {
        manager.logger.concat(2, LogLevel.Debug, logParts);
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
