package yanry.lib.java.model.schedule;

import java.util.ArrayList;

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
        if (setRunning(true)) {
            doRun();
            while (true) {
                ScheduleRunnable poll = manager.pendingRunnable.poll();
                if (poll != null) {
                    poll.doRun();
                } else {
                    break;
                }
            }
            setRunning(false);
        } else {
            manager.pendingRunnable.offer(this);
        }
    }

    private boolean setRunning(boolean running) {
        if (manager.isRunning.compareAndSet(!running, running)) {
            ArrayList<Scheduler> schedulers = new ArrayList<>(manager.instances.values());
            for (Scheduler scheduler : schedulers) {
                scheduler.visibility.setValue(scheduler.current != null);
            }
            return true;
        }
        return false;
    }

    protected abstract void doRun();
}
