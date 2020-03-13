package yanry.lib.java.model.process;

/**
 * Created by yanry on 2020/1/11.
 */
abstract class RequestRelay<D, R> extends RequestHook<D, R> {
    private RequestRoot<?, R> requestRoot;
    private boolean isFail;

    RequestRelay(RequestHook<?, R> parent, Processor<D, R> processor, RequestRoot<?, R> requestRoot) {
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
            if (startTime > 0 && requestRoot.logger != null && parent.isOpen()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (isTimeout) {
                    requestRoot.logger.dd(this, " timeout: ", elapsedTime);
                } else {
                    requestRoot.logger.vv(this, " pass: ", elapsedTime);
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
