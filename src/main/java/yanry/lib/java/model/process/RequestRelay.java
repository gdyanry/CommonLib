package yanry.lib.java.model.process;

/**
 * Created by yanry on 2020/1/11.
 */
abstract class RequestRelay<D, R> extends RequestHook<D, R> {
    private RequestRoot<?, R> requestRoot;
    private boolean isFail;

    RequestRelay(String fullName, RequestRoot<?, R> requestRoot) {
        super(fullName);
        this.requestRoot = requestRoot;
    }

    @Override
    public boolean isOpen() {
        return requestRoot.isOpen() && !isFail;
    }

    @Override
    protected boolean fail(boolean isTimeout) {
        if (isOpen()) {
            isFail = true;
            if (requestRoot.logger != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (isTimeout) {
                    requestRoot.logger.dd(fullName, " timeout: ", elapsedTime);
                } else {
                    requestRoot.logger.vv(fullName, " pass: ", elapsedTime);
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
