package yanry.lib.java.model.process;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2020/1/10.
 */
public interface Processor<D, R> {
    default ProcessRequest<D, R> request(Logger logger, D requestData, ProcessCallback<R> completeCallback) {
        String shortName = getShortName();
        if (logger != null) {
            logger.concat(1, LogLevel.Debug, shortName, " start request: ", requestData);
        }
        RequestRoot<D, R> requestRoot = new RequestRoot<>(shortName, logger, requestData, completeCallback);
        requestRoot.process(this);
        return requestRoot;
    }

    default long getTimeout() {
        return 0;
    }

    default boolean isEnable() {
        return true;
    }

    default String getShortName() {
        return getClass().getSimpleName();
    }

    void process(RequestHook<D, R> request);
}
