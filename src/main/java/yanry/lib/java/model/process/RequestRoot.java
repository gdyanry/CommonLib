package yanry.lib.java.model.process;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2020/1/11.
 */
final class RequestRoot<D, R> extends RequestHook<D, R> {
    final Logger logger;
    final D requestData;
    private ProcessCallback<R> completeCallback;
    private AtomicBoolean open;
    private HashMap<RequestHook<D, R>, TimerTask> pendingTimeout;

    RequestRoot(String fullName, Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        super(fullName);
        this.logger = logger;
        this.requestData = requestData;
        this.completeCallback = completeCallback;
        open = new AtomicBoolean(true);
        pendingTimeout = new HashMap<>();
    }

    void setTimeout(RequestHook<D, R> requestHook, long timeout) {
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

    void cancelTimeout(RequestHook<D, R> requestHook) {
        TimerTask timerTask = pendingTimeout.remove(requestHook);
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    boolean hit(RequestHook<D, R> requestHook, R result) {
        if (open.compareAndSet(true, false)) {
            if (logger != null) {
                long now = System.currentTimeMillis();
                logger.d("%s hit %s/%s: %s", requestHook.fullName, now - requestHook.startTime, now - startTime, result);
            }
            clearTimeout();
            if (completeCallback != null) {
                completeCallback.onSuccess(result);
            }
            return true;
        }
        return false;
    }

    private void clearTimeout() {
        for (TimerTask timerTask : pendingTimeout.values()) {
            timerTask.cancel();
        }
        pendingTimeout.clear();
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    protected boolean fail(boolean isTimeout) {
        if (open.compareAndSet(true, false)) {
            if (logger != null) {
                logger.dd(fullName, isTimeout ? " timeout: " : " fail: ", System.currentTimeMillis() - startTime);
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
