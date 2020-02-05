package yanry.lib.java.model.process.extend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import yanry.lib.java.model.process.Processor;
import yanry.lib.java.model.process.RequestHook;

/**
 * 根据请求数据的标签值快速查找子处理器的处理器分发器。
 * <p>
 * Created by yanry on 2020/1/29.
 *
 * @param <D> type of request data.
 * @param <R> type of process result.
 * @param <T> type of tag.
 */
public abstract class TagProcessorDispatcher<D, R, T> extends HashMap<T, LinkedList<Processor<D, R>>> implements Processor<D, R> {
    /**
     * 添加子处理器。
     *
     * @param tag       子处理器对应的标签值，若为null则表示通用处理器。
     * @param processor
     */
    public void addChildProcessor(T tag, Processor<D, R> processor) {
        LinkedList<Processor<D, R>> processors = get(tag);
        if (processors == null) {
            processors = new LinkedList<>();
            put(tag, processors);
        }
        processors.add(processor);
    }

    /**
     * 添加子处理器并映射到多个标签。
     *
     * @param processor
     * @param tags      子处理器对应的标签值。
     */
    public void addChildProcessor(Processor<D, R> processor, T... tags) {
        if (tags == null || tags.length == 0) {
            addChildProcessor(null, processor);
        } else {
            for (T tag : tags) {
                addChildProcessor(tag, processor);
            }
        }
    }

    protected abstract T getTag(D requestData);

    @Override
    public final void process(RequestHook<D, R> request) {
        T tag = getTag(request.getRequestData());
        LinkedList<Processor<D, R>> processors = get(tag);
        if (tag == null) {
            dispatch(request, processors);
        } else {
            LinkedList<Processor<D, R>> commonProcessors = get(null);
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
