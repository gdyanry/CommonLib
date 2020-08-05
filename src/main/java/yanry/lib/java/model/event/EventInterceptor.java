package yanry.lib.java.model.event;

/**
 * 事件拦截处理接口，采用View事件分发类似的机制，先注册的优先拦截，后注册的优先处理
 *
 * @author: rongyu.yan
 * @create: 2020-07-25 15:28
 **/
public interface EventInterceptor<E extends Event> {

    boolean isEnable();

    /**
     * 拦截事件查询
     *
     * @param event
     * @return 拦截层数，0表示不拦截，1表示拦截至上一级，2表示拦截至上两级，依此类推
     */
    int onDispatchEvent(E event);

    /**
     * 处理事件
     *
     * @param event
     * @return @see {@link #onDispatchEvent(Event)}
     */
    int onEvent(E event);
}
