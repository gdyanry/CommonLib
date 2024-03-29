package yanry.lib.java.model.event;

import yanry.lib.java.model.uml.UmlElement;

/**
 * 事件拦截处理接口，采用View事件分发类似的机制，先注册的优先拦截，后注册的优先处理
 *
 * @author: rongyu.yan
 * @create: 2020-07-25 15:28
 **/
public interface EventInterceptor<E extends Event> {

    default boolean isEnable() {
        return true;
    }

    /**
     * 拦截事件查询
     *
     * @param event
     * @return 拦截层数，0表示不拦截，1表示拦截至上一级，2表示拦截至上两级，依此类推
     */
    @UmlElement(note = "拦截")
    int onDispatchEvent(E event);

    /**
     * 处理事件
     *
     * @param event
     * @return @see {@link #onDispatchEvent(Event)}
     */
    @UmlElement(note = "处理")
    int onEvent(E event);
}
