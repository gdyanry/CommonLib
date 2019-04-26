package yanry.lib.java.model.task;

import yanry.lib.java.entity.DaemonTimer;
import yanry.lib.java.model.Singletons;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NonblockingCountDownLatch {
    public static final int STATE_UNTOUCHED = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAIL = 2;
    public static final int STATE_TIMEOUT = 3;

    private AtomicInteger counter;
    private TimerTask timeoutTask;

    public NonblockingCountDownLatch(int count) {
        this.counter = new AtomicInteger(count);
    }

    public void setTimeout(long timeout) {
        if (timeout > 0) {
            if (counter.get() > 0) {
                cleanTimeout();
                timeoutTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (counter.getAndSet(0) > 0) {
                            onComplete(STATE_TIMEOUT);
                        }
                    }
                };
                Singletons.get(DaemonTimer.class).schedule(timeoutTask, timeout);
            }
        }
    }

    private void cleanTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }

    public boolean isActive() {
        return counter.get() > 0;
    }

    public void countDown() {
        if (counter.decrementAndGet() == 0) {
            onComplete(STATE_UNTOUCHED);
            cleanTimeout();
        }
    }

    public void successInterrupt() {
        if (counter.getAndSet(0) > 0) {
            onComplete(STATE_SUCCESS);
            cleanTimeout();
        }
    }

    public void failInterrupt() {
        if (counter.getAndSet(0) > 0) {
            onComplete(STATE_FAIL);
            cleanTimeout();
        }
    }

    protected abstract void onComplete(int state);
}
