package yanry.lib.java.model.task;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

import yanry.lib.java.model.log.Logger;

/**
 * 单线程Executor，支持简单的优先级。
 * <p>
 * Created by yanry on 2020/2/21.
 */
public class SingleThreadExecutor extends Thread implements Executor {
    private boolean terminated;
    private BlockingDeque<Runnable> deque;

    public SingleThreadExecutor(String name) {
        super(name);
        deque = new LinkedBlockingDeque<>();
    }

    public SingleThreadExecutor(ThreadGroup group, String name) {
        super(group, name);
        deque = new LinkedBlockingDeque<>();
    }

    /**
     * 结束线程。
     */
    public void terminate() {
        terminated = true;
        interrupt();
    }

    public void enqueue(Runnable task, boolean urgent) {
        if (terminated) {
            throw new IllegalStateException("decoder is terminated.");
        }
        if (urgent) {
            deque.offerFirst(task);
        } else {
            deque.offerLast(task);
        }
    }

    @Override
    public void run() {
        Logger.getDefault().dd(getName(), '@', getId(), " started.");
        while (!terminated) {
            try {
                deque.takeFirst().run();
            } catch (InterruptedException e) {
                Logger.getDefault().catches(e);
            }
        }
        Logger.getDefault().dd(getName(), '@', getId(), " terminated.");
    }

    @Override
    public void execute(Runnable runnable) {
        enqueue(runnable, false);
    }
}
