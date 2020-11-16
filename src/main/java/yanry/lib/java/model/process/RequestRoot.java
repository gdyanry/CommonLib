package yanry.lib.java.model.process;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.runner.Runner;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yanry on 2020/1/11.
 */
final class RequestRoot<D, R extends ProcessResult> extends RequestHook<D, R> {
    private Runner runner;
    final Logger logger;
    final D requestData;
    private ProcessCallback<R> completeCallback;
    private AtomicBoolean open;
    private ConcurrentLinkedQueue<RequestHook<?, R>> pendingTimeout;

    public RequestRoot(Processor<D, R> processor, Runner runner, Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        super(null, processor);
        this.runner = runner;
        this.logger = logger;
        this.requestData = requestData;
        this.completeCallback = completeCallback;
        open = new AtomicBoolean(true);
        pendingTimeout = new ConcurrentLinkedQueue<>();
    }

    void setTimeout(RequestHook<?, R> requestHook, long timeout) {
        if (runner != null) {
            pendingTimeout.add(requestHook);
            runner.schedule(requestHook, timeout);
        }
    }

    void cancelTimeout(RequestHook<?, R> requestHook) {
        if (runner != null) {
            pendingTimeout.remove(requestHook);
            runner.cancel(requestHook);
        }
    }

    boolean hit(RequestHook<?, R> requestHook, R result) {
        if (open.compareAndSet(true, false)) {
            result.end(requestHook);
            if (logger != null) {
                logger.format(LogLevel.Debug, "%s hit %sms/%sms: %s", requestHook, result.getEndTime() - requestHook.getStartTime(), result.getElapsedTime(), result);
            }
            clearTimeout();
            RequestHook<?, R> hook = requestHook;
            while (hook != null) {
                hook.dispatchHit(result);
                hook = hook.parent;
            }
            if (completeCallback != null) {
                completeCallback.onSuccess(result);
            }
            return true;
        } else {
            cancelTimeout(requestHook);
            return false;
        }
    }

    private void clearTimeout() {
        if (runner != null) {
            for (RequestHook<?, R> hook : pendingTimeout) {
                runner.cancel(hook);
            }
        }
        pendingTimeout.clear();
    }

    @Override
    public D getRequestData() {
        return requestData;
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    protected boolean fail(boolean isTimeout) {
        if (open.compareAndSet(true, false)) {
            if (logger != null) {
                logger.concat(LogLevel.Debug, this, isTimeout ? " timeout: " : " fail: ", System.currentTimeMillis() - getStartTime(), "ms");
            }
            clearTimeout();
            if (completeCallback != null) {
                completeCallback.onFail(isTimeout);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hit(R result) {
        return hit(this, result);
    }

    @Override
    protected RequestRoot<D, R> getRequestRoot() {
        return this;
    }
}
