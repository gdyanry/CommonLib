package yanry.lib.java.model.process;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2020/1/11.
 */
final class RequestRoot<D, R extends ProcessResult> extends RequestHook<D, R> {
    final Logger logger;
    final D requestData;
    private ProcessCallback<R> completeCallback;
    private AtomicBoolean open;
    private HashMap<RequestHook<?, R>, TimerTask> pendingTimeout;

    public RequestRoot(Processor<D, R> processor, Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        super(null, processor);
        this.logger = logger;
        this.requestData = requestData;
        this.completeCallback = completeCallback;
        open = new AtomicBoolean(true);
        pendingTimeout = new HashMap<>();
    }

    void setTimeout(RequestHook<?, R> requestHook, long timeout) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                pendingTimeout.remove(requestHook);
                requestHook.fail(true);
            }
        };
        pendingTimeout.put(requestHook, timerTask);
        Singletons.get(Timer.class).schedule(timerTask, timeout);
    }

    void cancelTimeout(RequestHook<?, R> requestHook) {
        TimerTask timerTask = pendingTimeout.remove(requestHook);
        if (timerTask != null) {
            timerTask.cancel();
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
        for (TimerTask timerTask : pendingTimeout.values()) {
            timerTask.cancel();
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
