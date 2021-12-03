package yanry.lib.java.model.process.extend;

import yanry.lib.java.model.Registry;
import yanry.lib.java.model.process.ProcessResult;
import yanry.lib.java.model.process.Processor;
import yanry.lib.java.model.process.RequestHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
public abstract class TagProcessorDispatcher<D, R extends ProcessResult, T> implements Processor<D, R> {
    private HashMap<T, Registry<Processor<? super D, R>>> processorHolder = new HashMap<>();
    private Comparator<Processor<? super D, R>> comparator;

    /**
     * 设置排序比较器，设置后再调用addChildProcessor()时生效。
     *
     * @param comparator
     */
    public void setComparator(Comparator<Processor<? super D, R>> comparator) {
        this.comparator = comparator;
        for (Registry<Processor<? super D, R>> registry : processorHolder.values()) {
            registry.setComparator(comparator);
        }
    }

    public Registry<Processor<? super D, R>> getProcessorRegistry(T tag) {
        Registry<Processor<? super D, R>> registry = processorHolder.get(tag);
        if (registry == null) {
            synchronized (tag == null ? this : tag) {
                registry = processorHolder.get(tag);
                if (registry == null) {
                    registry = new Registry<>();
                    registry.setComparator(comparator);
                    processorHolder.put(tag, registry);
                }
            }
        }
        return registry;
    }

    /**
     * 添加子处理器并映射到多个标签。
     *
     * @param processor
     * @param tags      子处理器对应的标签列表。
     */
    public void addChildProcessor(Processor<? super D, R> processor, T... tags) {
        if (tags == null || tags.length == 0) {
            getProcessorRegistry(null).register(processor);
        } else {
            for (T tag : tags) {
                getProcessorRegistry(tag).register(processor);
            }
        }
    }

    public void removeChildProcessor(Processor<? super D, R> processor) {
        for (Registry<Processor<? super D, R>> registry : processorHolder.values()) {
            registry.unregister(processor);
        }
    }

    protected abstract T getTag(D requestData);

    @Override
    public void process(RequestHandler<? extends D, R> request) {
        T tag = getTag(request.getRequestData());
        Registry<Processor<? super D, R>> registry = processorHolder.get(tag);
        List<Processor<? super D, R>> processors = registry == null ? null : registry.getList();
        if (tag == null) {
            dispatch(request, processors);
        } else {
            Registry<Processor<? super D, R>> defaultRegistry = processorHolder.get(null);
            List<Processor<? super D, R>> commonProcessors = defaultRegistry == null ? null : defaultRegistry.getList();
            if (commonProcessors == null) {
                dispatch(request, processors);
            } else if (processors == null) {
                dispatch(request, commonProcessors);
            } else {
                ArrayList<Processor<? super D, R>> combinedProcessors = new ArrayList<>(commonProcessors.size() + processors.size());
                combinedProcessors.addAll(processors);
                combinedProcessors.addAll(commonProcessors);
                dispatch(request, combinedProcessors);
            }
        }
    }

    private void dispatch(RequestHandler<? extends D, R> request, List<Processor<? super D, R>> processors) {
        if (processors == null) {
            request.fail();
        } else {
            request.dispatch(request.getRequestData(), processors, false);
        }
    }
}
