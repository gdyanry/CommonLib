package yanry.lib.java.model.process;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by yanry on 2020/1/11.
 */
public abstract class RequestHook<D, R> implements ProcessRequest<D, R> {
    final String fullName;
    long startTime;

    RequestHook(String fullName) {
        this.fullName = fullName;
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
