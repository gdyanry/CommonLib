package yanry.lib.java.model.process;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

/**
 * 请求处理器接口。
 * <p>
 * Created by yanry on 2020/1/10.
 */
public interface Processor<D, R> {
    /**
     * 通过当前处理器发起请求。
     *
     * @param logger           用于输出处理请求过程中的日志信息，若为null则不输出日志。
     * @param requestData      请求数据。
     * @param completeCallback 请求结果回调。
     * @return 当前请求对象。
     */
    default ProcessRequest<D, R> request(Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        String shortName = getShortName();
        if (logger != null) {
            logger.concat(1, LogLevel.Debug, shortName, " start request: ", requestData);
        }
        RequestRoot<D, R> requestRoot = new RequestRoot<>(shortName, logger, requestData, completeCallback);
        requestRoot.process(this);
        return requestRoot;
    }

    /**
     * @return 请求超时时间，默认为0，即不设置超时。
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
     * 处理请求。
     *
     * @param request 请求对象，处理请求时可用于查询请求数据和请求状态，分发处理，或者提交处理结果。
     */
    void process(RequestHook<D, R> request);
}
