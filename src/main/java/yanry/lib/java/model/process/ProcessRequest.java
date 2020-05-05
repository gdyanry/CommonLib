package yanry.lib.java.model.process;

import yanry.lib.java.model.log.Logger;

/**
 * 请求对象接口。该对象由客户端调用{@link Processor#request(Logger, Object, ProcessCallback)}后返回，客户端可通过该对象查询请求状态或者中断请求。
 * <p>
 * Created by yanry on 2020/1/12.
 */
public interface ProcessRequest<D, R extends ProcessResult> {
    D getRequestData();

    /**
     * @return if current request is in progress.
     */
    boolean isOpen();

    /**
     * 失败结束当前请求。
     *
     * @return 操作是否成功。
     */
    boolean fail();
}
