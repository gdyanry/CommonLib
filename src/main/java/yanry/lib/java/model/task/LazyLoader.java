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

    public LazyLoader(ExecutorService executorService) {
        future = executorService.submit(this);
    }

    @Override
    public T get() {
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
            try {
                return call();
            } catch (Exception ex) {
                Logger.getDefault().catches(ex);
            }
        }
        return null;
    }
}
