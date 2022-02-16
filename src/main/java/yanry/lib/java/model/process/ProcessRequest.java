package yanry.lib.java.model.process;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.runner.Runner;
import yanry.lib.java.model.uml.UmlElement;

/**
 * 请求对象接口。该对象由客户端调用{@link Processor#request(Runner, Logger, Object, ProcessCallback)}后返回，客户端可通过该对象查询请求状态或者中断请求。
 * <p>
 * Created by yanry on 2020/1/12.
 *
 * @param <D> 待处理的数据类型。
 */
@UmlElement(include = false)
public interface ProcessRequest<D> {
    D getRequestData();

    /**
     * @return if current request is in progress.
     */
    boolean isOpen();

    /**
     * 取消请求并触发失败回调。
     *
     * @return 操作是否成功。
     */
    boolean fail();
}
