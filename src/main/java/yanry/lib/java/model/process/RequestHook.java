package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 用对象用于处理器在处理某个请求的过程中查询请求数据和请求状态，以及提交处理结果。
 * <p>
 * Created by yanry on 2020/1/11.
 */
public abstract class RequestHook<D, R> implements ProcessRequest<D, R> {
    RequestHook<?, R> parent;
    private Processor<D, R> processor;
    private String fullName;
    long startTime;

    RequestHook(RequestHook<?, R> parent, Processor<D, R> processor) {
        while (parent != null && parent.processor.isAnonymous()) {
            parent = parent.parent;
        }
        this.parent = parent;
        this.processor = processor;
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
        new RequestRelay<T, R>(this, processor, getRequestRoot()) {
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
        }.process();
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
        LinkedList<? extends Processor<T, R>> remainingProcessors = new LinkedList<>(childProcessors);
        if (keepOrder) {
            dispatchInOrder(requestData, remainingProcessors);
        } else {
            for (Processor<T, R> processor : childProcessors) {
                new RequestRelay<T, R>(this, processor, getRequestRoot()) {
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
                }.process();
            }
        }
    }

    void process() {
        if (isOpen()) {
            if (processor.isEnable()) {
                if (!processor.isAnonymous() && getRequestRoot().logger != null) {
                    getRequestRoot().logger.vv(this, " enter.");
                    startTime = System.currentTimeMillis();
                }
                long timeout = processor.getTimeout();
                if (timeout > 0) {
                    getRequestRoot().setTimeout(this, timeout);
                }
                processor.process(this);
            } else {
                if (!processor.isAnonymous() && getRequestRoot().logger != null) {
                    getRequestRoot().logger.vv(this, " is disable.");
                }
                fail(false);
            }
        }
    }

    private <T> void dispatchInOrder(T requestData, LinkedList<? extends Processor<T, R>> remainingProcessors) {
        Processor<T, R> processor = remainingProcessors.peekFirst();
        new RequestRelay<T, R>(this, processor, getRequestRoot()) {
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
        }.process();
    }

    void dispatchHit(R result) {
        processor.onHit(getRequestData(), result);
    }

    protected abstract RequestRoot<?, R> getRequestRoot();

    protected abstract boolean fail(boolean isTimeout);

    @Override
    public final boolean fail() {
        return fail(false);
    }

    @Override
    public String toString() {
        if (fullName == null) {
            fullName = parent == null ? processor.getShortName() : processor.isAnonymous() ? parent.toString() : parent.toString() + '-' + processor.getShortName();
        }
        return fullName;
    }
}
