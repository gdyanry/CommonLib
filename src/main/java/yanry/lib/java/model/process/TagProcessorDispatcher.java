package yanry.lib.java.model.process;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 根据请求数据的标签值快速查找子处理器的处理器分发器。
 * <p>
 * Created by yanry on 2020/1/29.
 *
 * @param <D> type of request data.
 * @param <R> type of process result.
 * @param <T> type of tag.
 */
public abstract class TagProcessorDispatcher<D, R, T> implements Processor<D, R> {
    private HashMap<T, LinkedList<Processor<D, R>>> map;

    public TagProcessorDispatcher() {
        map = new HashMap<>();
    }

    public void addChildProcessor(T tag, Processor<D, R> childProcessor) {
        LinkedList<Processor<D, R>> processors = map.get(tag);
        if (processors == null) {
            processors = new LinkedList<>();
            map.put(tag, processors);
        }
        processors.add(childProcessor);
    }

    protected abstract T getTag(D requestData);

    @Override
    public final void process(RequestHook<D, R> request) {
        LinkedList<Processor<D, R>> processor = map.get(getTag(request.getRequestData()));
        if (processor == null) {
            request.fail();
        } else {
            request.dispatch(request.getRequestData(), processor, false);
        }
    }
}
