package yanry.lib.java.model.task;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

import java.util.concurrent.*;

/**
 * 封装并非马上需要使用的数据加载（异步加载）行为，当需要使用数据（调用{@link #get()}）的时候如果数据尚未加载完成，则会阻塞线程直至得到结果。
 *
 * @param <T> 加载的数据类型
 */
public abstract class LazyLoader<T> implements Callable<T>, Future<T> {
    private Future<T> future;

    public boolean startLoading(ExecutorService executorService) {
        if (future == null) {
            synchronized (this) {
                if (future == null) {
                    future = executorService.submit(this);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }
        logNotStarted();
        return false;
    }

    @Override
    public boolean isCancelled() {
        if (future != null) {
            return future.isCancelled();
        }
        logNotStarted();
        return false;
    }

    @Override
    public boolean isDone() {
        if (future != null) {
            return future.isDone();
        }
        logNotStarted();
        return false;
    }

    @Override
    public T get() {
        if (future == null) {
            logNotStarted();
        } else if (future.isCancelled()) {
            logCancelled();
        } else {
            try {
                if (future.isDone()) {
                    return future.get();
                } else {
                    long start = System.currentTimeMillis();
                    T target = future.get();
                    logCostTime(start, target);
                    return target;
                }
            } catch (ExecutionException | InterruptedException e) {
                Logger.getDefault().catches(e);
            } catch (CancellationException e) {
                logCancelled();
            }
        }
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        if (future == null) {
            logNotStarted();
        } else if (future.isCancelled()) {
            logCancelled();
        } else {
            try {
                if (future.isDone()) {
                    return future.get();
                } else {
                    long start = System.currentTimeMillis();
                    T target = future.get(timeout, unit);
                    logCostTime(start, target);
                    return target;
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Logger.getDefault().catches(e);
            } catch (CancellationException e) {
                logCancelled();
            }
        }
        return null;
    }

    private void logNotStarted() {
        Logger.getDefault().ww("lazy loader is not started yet: ", this);
    }

    private void logCancelled() {
        Logger.getDefault().dd("lazy loader is cancelled: ", this);
    }

    private void logCostTime(long start, T target) {
        Logger.getDefault().concat(LogLevel.Debug, "lazy loader waits ", System.currentTimeMillis() - start, "ms: ", target);
    }
}
