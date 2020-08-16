package yanry.lib.java.model.task;

import java.util.concurrent.atomic.AtomicInteger;

import yanry.lib.java.model.runner.Runner;

public abstract class NonblockingCountDownLatch implements Runnable {
    public static final int STATE_UNTOUCHED = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAIL = 2;
    public static final int STATE_TIMEOUT = 3;

    private AtomicInteger counter;
    private Runner runner;

    public NonblockingCountDownLatch(int count) {
        this.counter = new AtomicInteger(count);
    }

    public void setTimeout(Runner runner, long timeout) {
        if (timeout > 0 && counter.get() > 0) {
            this.runner = runner;
            runner.schedule(this, timeout);
        }
    }

    public boolean isActive() {
        return counter.get() > 0;
    }

    public void countDown() {
        if (counter.decrementAndGet() == 0) {
            onComplete(STATE_UNTOUCHED);
            runner.cancel(this);
        }
    }

    public void successInterrupt() {
        if (counter.getAndSet(0) > 0) {
            onComplete(STATE_SUCCESS);
            runner.cancel(this);
        }
    }

    public void failInterrupt() {
        if (counter.getAndSet(0) > 0) {
            onComplete(STATE_FAIL);
            runner.cancel(this);
        }
    }

    protected abstract void onComplete(int state);

    @Override
    public void run() {
        if (counter.getAndSet(0) > 0) {
            onComplete(STATE_TIMEOUT);
        }
    }
}
