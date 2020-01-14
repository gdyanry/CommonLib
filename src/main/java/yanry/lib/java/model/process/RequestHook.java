package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;

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
     * 将当前请求分发给多个子处理器处理。
     *
     * @param childProcessors 子处理器集合。
     * @param keepOrder       子处理器是否按顺序执行（即当前一个子处理器处理失败时后一个子处理才开始处理）。
     */
    public void dispatch(Collection<? extends Processor<D, R>> childProcessors, boolean keepOrder) {
        if (childProcessors.size() == 0) {
            fail();
            return;
        }
        LinkedList<? extends Processor<D, R>> remainingProcessors = new LinkedList<>(childProcessors);
        if (keepOrder) {
            dispatchInOrder(remainingProcessors);
        } else {
            for (Processor<D, R> processor : childProcessors) {
                new RequestRelay<D, R>(fullName + '-' + processor.getShortName(), getRequestRoot()) {
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

    void process(Processor<D, R> processor) {
        if (isOpen() && processor.isEnable()) {
            if (getRequestRoot().logger != null) {
                getRequestRoot().logger.vv(fullName, " enter.");
                startTime = System.currentTimeMillis();
            }
            long timeout = processor.getTimeout();
            if (timeout > 0) {
                getRequestRoot().setTimeout(this, timeout);
            }
            processor.process(this);
        }
    }

    private void dispatchInOrder(LinkedList<? extends Processor<D, R>> remainingProcessors) {
        Processor<D, R> processor = remainingProcessors.peekFirst();
        new RequestRelay<D, R>(fullName + '-' + processor.getShortName(), getRequestRoot()) {
            @Override
            protected void onPass() {
                if (RequestHook.this.isOpen() && remainingProcessors.remove(processor)) {
                    if (remainingProcessors.size() == 0) {
                        RequestHook.this.fail();
                    } else {
                        RequestHook.this.dispatchInOrder(remainingProcessors);
                    }
                }
            }
        }.process(processor);
    }

    protected abstract RequestRoot<D, R> getRequestRoot();

    protected abstract boolean fail(boolean isTimeout);

    @Override
    public final D getRequestData() {
        return getRequestRoot().requestData;
    }

    @Override
    public final boolean fail() {
        return fail(false);
    }

    @Override
    public String toString() {
        return fullName;
    }
}
