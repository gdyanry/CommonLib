package yanry.lib.java.t;

import yanry.lib.java.model.log.ConsoleHandler;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.SimpleFormatter;
import yanry.lib.java.model.schedule.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yanry on 2019/12/17.
 */
public class SchedulerTest {
    public static void main(String[] args) {
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().time().sequenceNumber().thread().method(), LogLevel.Verbose));
        SchedulerManager schedulerManager = new SchedulerManager(new TimerScheduleRunner("test-runner", false));
        Scheduler scheduler = schedulerManager.get("test");
        TestData data = new TestData("DURATION", false);
        data.setDuration(3000);
        scheduler.show(data, TestDisplay.class);

        TestData selfDismissData = new TestData("DISMISS", true);
        scheduler.show(selfDismissData, TestDisplay.class);
    }

    public static class TestData extends ShowData implements OnDataStateChangeListener {
        private boolean dismissSelf;

        public TestData(String name, boolean dismissSelf) {
            this.dismissSelf = dismissSelf;
            setExtra(name);
            addOnStateChangeListener(this);
            setStrategy(STRATEGY_APPEND_TAIL);
        }

        @Override
        public void onDataStateChange(int toState) {
            Logger.getDefault().d("%s change state from %s to %s", this, getState(), toState);
        }
    }

    public static class TestDisplay extends ReusableDisplay<TestData, Integer> {
        private AtomicInteger atomicInteger = new AtomicInteger();

        @Override
        protected Integer createView(TestData data) {
            return atomicInteger.incrementAndGet();
        }

        @Override
        protected void setData(Integer view, TestData data) {
            if (data.dismissSelf) {
                data.dismiss(0);
            }
        }

        @Override
        protected void showView(Integer view) {

        }

        @Override
        protected void dismiss(Integer view) {

        }

        @Override
        protected boolean isShowing(Integer view) {
            return true;
        }
    }
}
