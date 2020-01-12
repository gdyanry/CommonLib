package yanry.lib.java.model.process;

import java.util.concurrent.Executor;

/**
 * Created by yanry on 2020/1/10.
 */
public abstract class SyncProcessor<D, R> implements Processor<D, R> {

    protected Executor getExecutor() {
        return null;
    }

    protected abstract R process(D requestData);

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
        R result = process(hook.getRequestData());
        if (hook.isOpen()) {
            if (result == null) {
                hook.fail();
            } else {
                hook.hit(result);
            }
        }
    }
}
