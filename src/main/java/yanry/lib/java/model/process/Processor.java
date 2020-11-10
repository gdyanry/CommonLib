package yanry.lib.java.model.process;

import yanry.lib.java.interfaces.DataTransformer;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.runner.Runner;

/**
 * 请求处理器接口。
 * <p>
 * Created by yanry on 2020/1/10.
 *
 * @param <D> type of request data.
 * @param <R> type of process result.
 */
public interface Processor<D, R extends ProcessResult> {
    /**
     * 通过当前处理器发起请求。
     *
     * @param runner           用于处理超时
     * @param logger           用于输出处理请求过程中的日志信息，若为null则不输出日志。
     * @param requestData      请求数据。
     * @param completeCallback 请求结果回调。
     * @return 当前请求对象。
     */
    default ProcessRequest<D> request(Runner runner, Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        String shortName = getShortName();
        if (logger != null) {
            logger.concat(1, LogLevel.Debug, shortName, " start request: ", requestData);
        }
        RequestRoot<D, R> requestRoot = new RequestRoot<>(this, runner, logger, requestData, completeCallback);
        requestRoot.process();
        return requestRoot;
    }

    /**
     * 将自身包装成别的请求数据类型的处理器。
     *
     * @param dataTransformer 请求数据转换接口。
     * @param <T>             目标请求数据类型。
     * @return 目标处理器。
     */
    default <T> Processor<T, R> wrap(DataTransformer<T, D> dataTransformer) {
        return new Processor<T, R>() {
            @Override
            public void process(RequestHandler<? extends T, R> request) {
                request.redirect(dataTransformer.transform(request.getRequestData()), Processor.this);
            }

            @Override
            public boolean isAnonymous() {
                return true;
            }
        };
    }

    /**
     * 是否匿名。
     *
     * @return 若返回true则不会打印该处理器相关的日志。
     */
    default boolean isAnonymous() {
        return false;
    }

    /**
     * @return 处理超时时间，默认为0，即不设置超时。只有当调用根节点的{@link #request(Runner, Logger, Object, ProcessCallback)}时Runner不为null，超时才会生效
     */
    default long getTimeout() {
        return 0;
    }

    /**
     * @return 当前处理器是否可用。
     */
    default boolean isEnable() {
        return true;
    }

    /**
     * @return 当前处理器名称。
     */
    default String getShortName() {
        return getClass().getSimpleName();
    }

    /**
     * 当前处理器或子处理器成功处理请求的回调。
     *
     * @param requestData
     * @param result
     */
    default void onHit(D requestData, R result) {
    }

    /**
     * 当前处理器及其子处理器未能处理请求的回调。
     *
     * @param requestData
     * @param isTimeout
     */
    default void onPass(D requestData, boolean isTimeout) {
    }

    /**
     * 处理请求。
     *
     * @param request 请求对象，处理请求时可用于查询请求数据和请求状态，分发处理，或者提交处理结果。
     */
    void process(RequestHandler<? extends D, R> request);
}
