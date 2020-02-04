package yanry.lib.java.model.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    /**
     * 添加子处理器。
     *
     * @param tag            子处理器对应的标签值，若为null则表示通用处理器。
     * @param childProcessor
     */
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
        T tag = getTag(request.getRequestData());
        LinkedList<Processor<D, R>> processors = map.get(tag);
        if (tag == null) {
            dispatch(request, processors);
        } else {
            LinkedList<Processor<D, R>> commonProcessors = map.get(null);
            if (commonProcessors == null) {
                dispatch(request, processors);
            } else if (processors == null) {
                dispatch(request, commonProcessors);
            } else {
                ArrayList<Processor<D, R>> combinedProcessors = new ArrayList<>(commonProcessors.size() + processors.size());
                combinedProcessors.addAll(processors);
                combinedProcessors.addAll(commonProcessors);
                dispatch(request, combinedProcessors);
            }
        }
    }

    private void dispatch(RequestHook<D, R> request, List<Processor<D, R>> processors) {
        if (processors == null) {
            request.fail();
        } else {
            request.dispatch(request.getRequestData(), processors, false);
        }
    }
}
