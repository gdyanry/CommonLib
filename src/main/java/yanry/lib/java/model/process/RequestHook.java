package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 用对象用于处理器在处理某个请求的过程中查询请求数据和请求状态，以及提交处理结果。
 * <p>
 * Created by yanry on 2020/1/11.
 */
abstract class RequestHook<D, R extends ProcessResult> implements RequestHandler<D, R>, ProcessNode<D, R>, Runnable {
    RequestHook<?, R> parent;
    private Processor<? super D, R> processor;
    private String fullName;
    private long startTime;

    RequestHook(RequestHook<?, R> parent, Processor<? super D, R> processor) {
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
    public <T> void redirect(T requestData, Processor<? super T, R> processor) {
        if (requestData == null || processor == null) {
            fail();
            return;
        }
        new RequestRelay<T, R>(this, processor, getRequestRoot()) {
            @Override
            public T getRequestData() {
                return requestData;
            }

            @Override
            protected void onFail() {
                if (RequestHook.this.isOpen()) {
                    RequestHook.this.fail();
                }
            }
        }.process();
    }

    @Override
    public <T> void dispatch(T requestData, Collection<? extends Processor<? super T, R>> childProcessors, boolean keepOrder) {
        if (requestData == null || childProcessors == null || childProcessors.size() == 0) {
            fail();
            return;
        }
        LinkedList<? extends Processor<? super T, R>> remainingProcessors = new LinkedList<>(childProcessors);
        if (keepOrder) {
            dispatchInOrder(requestData, remainingProcessors);
        } else {
            for (Processor<? super T, R> processor : childProcessors) {
                if (processor == null) {
                    checkFail(remainingProcessors, null);
                } else {
                    new RequestRelay<T, R>(this, processor, getRequestRoot()) {
                        @Override
                        public T getRequestData() {
                            return requestData;
                        }

                        @Override
                        protected void onFail() {
                            RequestHook.this.checkFail(remainingProcessors, processor);
                        }
                    }.process();
                }
            }
        }
    }

    private <T> void dispatchInOrder(T requestData, LinkedList<? extends Processor<? super T, R>> remainingProcessors) {
        Processor<? super T, R> processor;
        while ((processor = remainingProcessors.peekFirst()) == null) {
            if (!checkFail(remainingProcessors, null)) {
                return;
            }
        }
        new RequestRelay<T, R>(this, processor, getRequestRoot()) {
            @Override
            public T getRequestData() {
                return requestData;
            }

            @Override
            protected void onFail() {
                if (RequestHook.this.checkFail(remainingProcessors, getProcessor())) {
                    RequestHook.this.dispatchInOrder(requestData, remainingProcessors);
                }
            }
        }.process();
    }

    /**
     * @param remainingProcessors
     * @param processorToRemove
     * @param <T>
     * @return 是否继续
     */
    private <T> boolean checkFail(LinkedList<? extends Processor<? super T, R>> remainingProcessors, Processor<? super T, R> processorToRemove) {
        if (isOpen()) {
            remainingProcessors.remove(processorToRemove);
            if (remainingProcessors.size() == 0) {
                // 所有子处理器均失败时触发当前处理器失败
                fail();
            } else {
                return true;
            }
        }
        return false;
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
    public Processor<? super D, R> getProcessor() {
        return processor;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void run() {
        fail(true);
    }

    @Override
    public String toString() {
        if (fullName == null) {
            fullName = parent == null ? processor.getShortName() : processor.isAnonymous() ? parent.toString() : parent.toString() + '-' + processor.getShortName();
        }
        return fullName;
    }
}
