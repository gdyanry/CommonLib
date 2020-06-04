package yanry.lib.java.model.process;

import java.util.Collection;

/**
 * 请求处理扩展接口，增加了输出请求结果、转发请求和重定向请求的方法。
 * <p>
 * Created by yanry on 2020/5/5.
 *
 * @param <D> 待处理的数据类型。
 * @param <R> 处理结果类型。
 */
public interface RequestHandler<D, R extends ProcessResult> extends ProcessRequest<D, R> {

    /**
     * 成功结束当前请求。
     *
     * @param result 请求结果。
     * @return 操作是否成功。
     */
    boolean hit(R result);

    /**
     * 将当前请求“重定向”给指定处理器处理。该方法和直接调用{@link Processor#process(RequestHandler)}的区别在于，使用后者时，被“重定向”的处理器
     * {@link Processor#isEnable()}、{@link Processor#getTimeout()}将不会生效，{@link Processor#getShortName()}也不会显示在日志中，就好像透明一样。
     * 因此不应当直接调用{@link Processor#process(RequestHandler)}。
     *
     * @param requestData 请求数据。
     * @param processor   被“重定向”的处理器。
     */
    <T> void redirect(T requestData, Processor<T, R> processor);

    /**
     * 将当前请求分发给多个子处理器处理。
     *
     * @param requestData     请求数据。
     * @param childProcessors 子处理器集合。
     * @param keepOrder       子处理器是否按顺序执行（即当前一个子处理器处理失败时后一个子处理才开始处理）。
     */
    <T> void dispatch(T requestData, Collection<? extends Processor<T, R>> childProcessors, boolean keepOrder);
}
