package yanry.lib.java.model.task;

import yanry.lib.java.model.log.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadSafeExecutor implements Runnable {
    private static final int STATE_PENDING = 0;
    private static final int STATE_WORKING = 1;
    private static final int STATE_ABORTING = 2;
    private LinkedBlockingQueue<Runnable> queue;
    private Thread workThread;
    private int state;

    public ThreadSafeExecutor() {
        queue = new LinkedBlockingQueue<>();
    }

    public <T> T sync(Callable<T> callable) {
        if (state == STATE_ABORTING) {
            throw new IllegalStateException("current state is " + getState());
        } else {
            try {
                if (Thread.currentThread().equals(workThread)) {
                    return callable.call();
                } else {
                    FutureTask<T> futureTask = new FutureTask<>(callable);
                    queue.put(futureTask);
                    return futureTask.get();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getState() {
        switch (state) {
            case STATE_ABORTING:
                return "ABORTING";
            case STATE_PENDING:
                return "PENDING";
            case STATE_WORKING:
                return "WORKING";
            default:
                return String.format("UNKNOWN(%s)", state);
        }
    }

    public void sync(Runnable runnable) {
        if (state == STATE_ABORTING) {
            throw new IllegalStateException("current state is " + getState());
        } else {
            if (Thread.currentThread().equals(workThread)) {
                runnable.run();
            } else {
                FutureTask<?> futureTask = new FutureTask<Void>(runnable, null);
                try {
                    queue.put(futureTask);
                    futureTask.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void async(Runnable runnable) {
        if (state == STATE_ABORTING) {
            throw new IllegalStateException("current state is " + getState());
        } else {
            try {
                queue.put(runnable);
            } catch (InterruptedException e) {
                Logger.getDefault().catches(e);
            }
        }
    }

    public void stop() {
        if (state == STATE_WORKING) {
            state = STATE_ABORTING;
            workThread.interrupt();
        }
    }

    @Override
    public void run() {
        if (state == STATE_PENDING) {
            state = STATE_WORKING;
            workThread = Thread.currentThread();
            while (state == STATE_WORKING) {
                try {
                    Runnable take = queue.take();
                    take.run();
                } catch (InterruptedException e) {
                    Logger.getDefault().ii("work thread is interrupted.");
                }
            }
            if (state == STATE_ABORTING) {
                if (queue.size() > 0) {
                    Logger.getDefault().vv(queue.size(), " tasks are aborted.");
                }
                queue.clear();
                workThread = null;
                state = STATE_PENDING;
            } else {
                Logger.getDefault().e("state is %s instead of %s(PENDING)", getState(), STATE_PENDING);
            }
        }
    }
}
