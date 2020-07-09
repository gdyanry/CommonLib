package yanry.lib.java.model.process;

import yanry.lib.java.model.log.LogLevel;

/**
 * Created by yanry on 2020/1/11.
 */
abstract class RequestRelay<D, R extends ProcessResult> extends RequestHook<D, R> {
    private RequestRoot<?, R> requestRoot;
    private boolean isFail;

    RequestRelay(RequestHook<?, R> parent, Processor<? super D, R> processor, RequestRoot<?, R> requestRoot) {
        super(parent, processor);
        this.requestRoot = requestRoot;
    }

    @Override
    public boolean isOpen() {
        return !isFail && parent.isOpen();
    }

    @Override
    protected boolean fail(boolean isTimeout) {
        if (!isFail) {
            isFail = true;
            if (requestRoot.logger != null && parent.isOpen()) {
                long elapsedTime = System.currentTimeMillis() - getStartTime();
                if (isTimeout) {
                    requestRoot.logger.dd(this, " timeout: ", elapsedTime, "ms");
                } else {
                    requestRoot.logger.concat(LogLevel.Verbose, this, " pass: ", elapsedTime, "ms");
                }
            }
            requestRoot.cancelTimeout(this);
            onPass();
            return true;
        }
        return false;
    }

    @Override
    public boolean hit(R result) {
        return !isFail && requestRoot.hit(this, result);
    }

    @Override
    protected RequestRoot<?, R> getRequestRoot() {
        return requestRoot;
    }

    protected abstract void onPass();
}
