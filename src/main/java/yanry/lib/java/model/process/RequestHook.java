package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import yanry.lib.java.model.log.LogLevel;

/**
 * 用对象用于处理器在处理某个请求的过程中查询请求数据和请求状态，以及提交处理结果。
 * <p>
 * Created by yanry on 2020/1/11.
 */
public abstract class RequestHook<D, R> implements ProcessRequest<D, R> {
    final String fullName;
    long startTime;

    RequestHook(String fullName) {
        this.fullName = fullName;
    }

    /**
     * 将当前请求“重定向”给指定处理器处理。该方法和直接调用{@link Processor#process(RequestHook)}的区别在于，使用后者时，被“重定向”的处理器
     * {@link Processor#isEnable()}、{@link Processor#getTimeout()}将不会生效，{@link Processor#getShortName()}也不会显示在日志中，就好像透明一样。
     * 因此不应当直接调用{@link Processor#process(RequestHook)}。
     *
     * @param requestData 请求数据。
     * @param processor   被“重定向”的处理器。
     */
    public <T> void redirect(T requestData, Processor<T, R> processor) {
        logTransformRequestData(requestData);
        new RequestRelay<T, R>(getRelayName(processor), getRequestRoot()) {
            @Override
            public T getRequestData() {
                return requestData;
            }

            @Override
            protected void onPass() {
                if (RequestHook.this.isOpen()) {
                    RequestHook.this.fail();
                }
            }
        }.process(processor);
    }

    /**
     * 将当前请求分发给多个子处理器处理。
     *
     * @param requestData     请求数据。
     * @param childProcessors 子处理器集合。
     * @param keepOrder       子处理器是否按顺序执行（即当前一个子处理器处理失败时后一个子处理才开始处理）。
     */
    public <T> void dispatch(T requestData, Collection<? extends Processor<T, R>> childProcessors, boolean keepOrder) {
        if (childProcessors.size() == 0) {
            fail();
            return;
        }
        logTransformRequestData(requestData);
        LinkedList<? extends Processor<T, R>> remainingProcessors = new LinkedList<>(childProcessors);
        if (keepOrder) {
            dispatchInOrder(requestData, remainingProcessors);
        } else {
            for (Processor<T, R> processor : childProcessors) {
                new RequestRelay<T, R>(getRelayName(processor), getRequestRoot()) {
                    @Override
                    public T getRequestData() {
                        return requestData;
                    }

                    @Override
                    protected void onPass() {
                        if (RequestHook.this.isOpen() && remainingProcessors.remove(processor) && remainingProcessors.size() == 0) {
                            RequestHook.this.fail();
                        }
                    }
                }.process(processor);
            }
        }
    }

    private <T> String getRelayName(Processor<T, R> processor) {
        return processor.isAnonymous() ? fullName : fullName + '-' + processor.getShortName();
    }

    private <T> void logTransformRequestData(T transformedData) {
        if (getRequestRoot().logger != null && !Objects.equals(transformedData, getRequestData())) {
            getRequestRoot().logger.concat(2, LogLevel.Verbose, "transform request data: ", getRequestData(), " -> ", transformedData);
        }
    }

    void process(Processor<D, R> processor) {
        if (isOpen()) {
            if (processor.isEnable()) {
                if (!processor.isAnonymous() && getRequestRoot().logger != null) {
                    getRequestRoot().logger.vv(fullName, " enter.");
                    startTime = System.currentTimeMillis();
                }
                long timeout = processor.getTimeout();
                if (timeout > 0) {
                    getRequestRoot().setTimeout(this, timeout);
                }
                processor.process(this);
            } else {
                if (!processor.isAnonymous() && getRequestRoot().logger != null) {
                    getRequestRoot().logger.vv(fullName, " is disable.");
                }
                fail(false);
            }
        }
    }

    private <T> void dispatchInOrder(T requestData, LinkedList<? extends Processor<T, R>> remainingProcessors) {
        Processor<T, R> processor = remainingProcessors.peekFirst();
        new RequestRelay<T, R>(getRelayName(processor), getRequestRoot()) {
            @Override
            public T getRequestData() {
                return requestData;
            }

            @Override
            protected void onPass() {
                if (RequestHook.this.isOpen() && remainingProcessors.remove(processor)) {
                    if (remainingProcessors.size() == 0) {
                        RequestHook.this.fail();
                    } else {
                        RequestHook.this.dispatchInOrder(requestData, remainingProcessors);
                    }
                }
            }
        }.process(processor);
    }

    protected abstract RequestRoot<?, R> getRequestRoot();

    protected abstract boolean fail(boolean isTimeout);

    @Override
    public final boolean fail() {
        return fail(false);
    }

    @Override
    public String toString() {
        return fullName;
    }
}
