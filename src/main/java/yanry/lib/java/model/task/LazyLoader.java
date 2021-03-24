package yanry.lib.java.model.task;

import yanry.lib.java.interfaces.Supplier;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 封装并非马上需要使用的数据加载（异步加载）行为，当需要使用数据（调用{@link #get()}）的时候如果数据尚未加载完成，则会阻塞线程直至得到结果。
 *
 * @param <T> 加载的数据类型
 */
public abstract class LazyLoader<T> implements Callable<T>, Supplier<T> {
    private Future<T> future;

    public boolean startLoading(ExecutorService executorService) {
        if (future == null) {
            future = executorService.submit(this);
            return true;
        }
        return false;
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason.  If the task has already started,
     * then the {@code mayInterruptIfRunning} parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }
        return false;
    }

    @Override
    public T get() {
        if (future == null) {
            Logger.getDefault().ww("lazy loader is not started yet: ", this);
        } else {
            try {
                if (future.isDone()) {
                    return future.get();
                } else {
                    long start = System.currentTimeMillis();
                    T target = future.get();
                    Logger.getDefault().concat(LogLevel.Debug, "lazy loader waits ", System.currentTimeMillis() - start, "ms: ", target);
                    return target;
                }
            } catch (ExecutionException e) {
                Logger.getDefault().catches(e);
            } catch (InterruptedException e) {
                Logger.getDefault().catches(e);
                if (future.isCancelled()) {
                    Logger.getDefault().dd("lazy loader is cancel: ", this);
                } else {
                    try {
                        return call();
                    } catch (Exception ex) {
                        Logger.getDefault().catches(ex);
                    }
                }
            }
        }
        return null;
    }
}
