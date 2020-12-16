package yanry.lib.java.model.task;

import yanry.lib.java.interfaces.Supplier;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
