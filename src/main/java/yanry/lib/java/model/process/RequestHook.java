package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 用对象用于处理器在处理某个请求的过程中查询请求数据和请求状态，以及提交处理结果。
 * <p>
 * Created by yanry on 2020/1/11.
 */
public abstract class RequestHook<D, R extends ProcessResult> implements RequestHandler<D, R>, ProcessNode<D, R> {
    RequestHook<?, R> parent;
    private Processor<D, R> processor;
    private String fullName;
    private long startTime;

    RequestHook(RequestHook<?, R> parent, Processor<D, R> processor) {
        while (parent != null && parent.processor.isAnonymous()) {
            parent = parent.parent;
        }
        this.parent = parent;
        this.processor = processor;
    }

    void process() {
        if (isOpen()) {
            if (processor.isEnable()) {
                startTime = System.currentTimeMillis();
                if (!processor.isAnonymous() && getRequestRoot().logger != null) {
                    getRequestRoot().logger.vv(this, " enter.");
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

    @Override
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
                            // 所有子处理器均失败时触发当前处理器失败
                            RequestHook.this.fail();
                        }
                    }
                }.process();
            }
        }
    }

    @Override
    public ProcessNode<?, R> getParent() {
        return parent;
    }

    @Override
    public ProcessNode<?, R> getRoot() {
        return getRequestRoot();
    }

    @Override
    public Processor<D, R> getProcessor() {
        return processor;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        if (fullName == null) {
            fullName = parent == null ? processor.getShortName() : processor.isAnonymous() ? parent.toString() : parent.toString() + '-' + processor.getShortName();
        }
        return fullName;
    }
}
