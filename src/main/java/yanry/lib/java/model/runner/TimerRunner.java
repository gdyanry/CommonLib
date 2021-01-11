package yanry.lib.java.model.runner;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yanry on 2019/12/17.
 */
public class TimerRunner extends Timer implements Runner {
    private HashMap<Runnable, TimerTask> tasks = new HashMap<>();
    private Thread timerThread;

    public TimerRunner(String name, boolean isDaemon) {
        super(name, isDaemon);
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
            schedule(runnable, 0);
        }
    }

    @Override
    public void schedule(Runnable runnable, long delay) {
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
    public void cancel(Runnable runnable) {
        TimerTask timerTask = tasks.remove(runnable);
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    @Override
    public void terminate() {
        cancel();
    }
}
