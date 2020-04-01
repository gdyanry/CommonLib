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
        if (manager.logger != null) {
            manager.logger.concat(2, LogLevel.Debug, logParts);
        }
        manager.runner.run(this);
    }

    @Override
    public void run() {
        if (manager.isRunning.setValue(true)) {
            doRun();
            while (true) {
                ScheduleRunnable poll = manager.pendingRunnable.poll();
                if (poll != null) {
                    poll.doRun();
                } else {
                    break;
                }
            }
            manager.isRunning.setValue(false);
        } else {
            manager.pendingRunnable.offer(this);
        }
    }

    protected abstract void doRun();
}
