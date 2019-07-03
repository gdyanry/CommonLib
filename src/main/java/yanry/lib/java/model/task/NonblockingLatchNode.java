package yanry.lib.java.model.task;

public class NonblockingLatchNode extends NonblockingCountDownLatch {
    private NonblockingCountDownLatch parentLatch;

    public NonblockingLatchNode(NonblockingCountDownLatch parentLatch) {
        this(1, parentLatch);
    }

    public NonblockingLatchNode(int weight, NonblockingCountDownLatch parentLatch) {
        super(weight);
        this.parentLatch = parentLatch;
    }

    public void successInterruptParent() {
        parentLatch.successInterrupt();
    }

    public void failInterruptParent() {
        parentLatch.failInterrupt();
    }

    @Override
    protected void onComplete(int state) {
        parentLatch.countDown();
    }
}
