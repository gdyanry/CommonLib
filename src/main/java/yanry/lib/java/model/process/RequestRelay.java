package yanry.lib.java.model.process;

/**
 * Created by yanry on 2020/1/11.
 */
abstract class RequestRelay<D, R> extends RequestHook<D, R> {
    private RequestRoot<D, R> requestRoot;
    private boolean isFail;

    RequestRelay(String fullName, RequestRoot<D, R> requestRoot) {
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
                requestRoot.logger.vv(fullName, isTimeout ? " timeout: " : " pass: ", System.currentTimeMillis() - startTime);
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
    protected RequestRoot<D, R> getRequestRoot() {
        return requestRoot;
    }

    protected abstract void onPass();
}
