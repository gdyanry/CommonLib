package yanry.lib.java.model.schedule.imple;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import yanry.lib.java.model.schedule.ScheduleRunner;

/**
 * Created by yanry on 2019/12/17.
 */
public class TimerScheduleRunner extends Timer implements ScheduleRunner {
    private HashMap<Runnable, TimerTask> tasks;
    private Thread timerThread;

    public TimerScheduleRunner(String name, boolean isDaemon) {
        super(name, isDaemon);
        tasks = new HashMap<>();
        schedule(new TimerTask() {
            @Override
            public void run() {
                timerThread = Thread.currentThread();
            }
        }, 0);
    }

    @Override
    public void run(Runnable runnable) {
        if (Thread.currentThread() == timerThread) {
            runnable.run();
        } else {
            scheduleTimeout(runnable, 0);
        }
    }

    @Override
    public void scheduleTimeout(Runnable runnable, long delay) {
        TimerTask old = tasks.get(runnable);
        if (old != null) {
            old.cancel();
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                tasks.remove(runnable);
                runnable.run();
            }
        };
        tasks.put(runnable, timerTask);
        schedule(timerTask, delay);
    }

    @Override
    public void cancelPendingTimeout(Runnable runnable) {
        TimerTask timerTask = tasks.remove(runnable);
        if (timerTask != null) {
            timerTask.cancel();
        }
    }
}
