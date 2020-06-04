package yanry.lib.java.model.process.extend;

import java.util.concurrent.Executor;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.process.ProcessResult;
import yanry.lib.java.model.process.Processor;
import yanry.lib.java.model.process.RequestHandler;

/**
 * 朴素处理器，该类主要是将{@link Processor#process(RequestHandler)}中提交结果的异步方式改为同步方式，使用起来更简单直观。
 * <p>
 * Created by yanry on 2020/1/10.
 *
 * @param <D> 待处理的数据类型。
 * @param <R> 处理结果类型。
 */
public abstract class PlainProcessor<D, R extends ProcessResult> implements Processor<D, R> {

    /**
     * 如果需要线程池或指定线程中处理请求，可重写此方法。
     *
     * @return
     */
    protected Executor getExecutor() {
        return null;
    }

    /**
     * 处理请求。
     *
     * @param requestData 请求数据。
     * @return 返回处理结果。
     * @throws Exception 处理请求过程中可能抛出的任何异常。
     */
    protected abstract R process(D requestData) throws Exception;

    @Override
    public final void process(RequestHandler<D, R> hook) {
        Executor executor = getExecutor();
        if (executor == null) {
            doProcess(hook);
        } else {
            executor.execute(() -> doProcess(hook));
        }
    }

    private void doProcess(RequestHandler<D, R> hook) {
        R result = null;
        try {
            result = process(hook.getRequestData());
        } catch (Exception e) {
            Logger.getDefault().catches(e);
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
