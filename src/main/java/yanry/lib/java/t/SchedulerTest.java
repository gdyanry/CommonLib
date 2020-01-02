package yanry.lib.java.t;

import java.util.concurrent.atomic.AtomicInteger;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.model.schedule.OnDataStateChangeListener;
import yanry.lib.java.model.schedule.ReusableDisplay;
import yanry.lib.java.model.schedule.Scheduler;
import yanry.lib.java.model.schedule.SchedulerManager;
import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.TimerScheduleRunner;

/**
 * Created by yanry on 2019/12/17.
 */
public class SchedulerTest {
    public static void main(String[] args) {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.TIME).addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.METHOD)
                .addFlag(SimpleFormatter.THREAD).addFlag(SimpleFormatter.PROCESS).addFlag(SimpleFormatter.LEVEL);
//        formatter.setMethodStack(5);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        Logger.getDefault().addHandler(handler);
        SchedulerManager schedulerManager = new SchedulerManager(new TimerScheduleRunner("schedule-runner", false), Logger.getDefault());
        schedulerManager.addSchedulerWatcher((scheduler, isVisible) -> Logger.getDefault().ii(scheduler, " is visible: ", isVisible));
        Scheduler scheduler = schedulerManager.get("testScheduler");
        TestData data = new TestData("DURATION", false);
        data.setDuration(3000);
        scheduler.show(data, TestDisplay.class);

        TestData selfDismissData = new TestData("DISMISS", true);
        scheduler.show(selfDismissData, TestDisplay.class);
        selfDismissData.addOnStateChangeListener(toState -> {
            if (toState == ShowData.STATE_DISMISS) {
                scheduler.show(data, TestDisplay.class);
            }
        });
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
            Logger.getDefault().w("%s change state from %s to %s", this, getState(), toState);
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
