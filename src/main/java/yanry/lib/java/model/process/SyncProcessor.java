package yanry.lib.java.model.process;

import java.util.concurrent.Executor;

import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2020/1/10.
 */
public abstract class SyncProcessor<D, R> implements Processor<D, R> {

    protected Executor getExecutor() {
        return null;
    }

    protected abstract R process(D requestData) throws Exception;

    @Override
    public final void process(RequestHook<D, R> hook) {
        Executor executor = getExecutor();
        if (executor == null) {
            doProcess(hook);
        } else {
            executor.execute(() -> doProcess(hook));
        }
    }

    private void doProcess(RequestHook<D, R> hook) {
        R result = null;
        try {
            result = process(hook.getRequestData());
        } catch (Exception e) {
            Logger logger = hook.getRequestRoot().logger;
            if (logger == null) {
                e.printStackTrace();
            } else {
                logger.catches(e);
            }
        } finally {
            if (hook.isOpen()) {
                if (result == null) {
                    hook.fail();
                } else {
                    hook.hit(result);
                }
            }
        }
    }
}
