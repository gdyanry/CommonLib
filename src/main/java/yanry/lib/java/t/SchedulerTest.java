package yanry.lib.java.t;

import java.util.concurrent.atomic.AtomicInteger;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.model.runner.TimerRunner;
import yanry.lib.java.model.schedule.Scheduler;
import yanry.lib.java.model.schedule.SchedulerManager;
import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.imple.ReusableDisplay;
import yanry.lib.java.model.watch.ValueWatcher;

/**
 * Created by yanry on 2019/12/17.
 */
public class SchedulerTest {
    public static void main(String[] args) {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.TIME).addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.METHOD)
                .addFlag(SimpleFormatter.THREAD).addFlag(SimpleFormatter.LEVEL);
//        formatter.setMethodStack(10);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        Logger.getDefault().addHandler(handler);

        SchedulerManager schedulerManager = new SchedulerManager(new TimerRunner("schedule-runner", false), Logger.getDefault());
        Scheduler scheduler = schedulerManager.get("testScheduler");
        scheduler.getVisibility().addWatcher(newValue -> Logger.getDefault().ii(scheduler, " is visible: ", newValue));
        // DURATION显示三秒
        TestData durationData = new TestData("DURATION");
        durationData.setDuration(3000);
        scheduler.show(durationData, TestDisplay.class);

        TestData selfDismissData = new TestData("DISMISS") {
            @Override
            protected void onStateChange(int to, int from) {
                if (to == ShowData.STATE_SHOWING) {
                    dismiss(0);
                }
            }
        };
        selfDismissData.setDuration(3000);
        scheduler.show(selfDismissData, TestDisplay.class);
        selfDismissData.getState().addWatcher((newValue, oldValue) -> {
            if (newValue == ShowData.STATE_DISMISS) {
                scheduler.show(durationData, TestDisplay.class);
            }
        });
    }

    public static class TestData extends ShowData implements ValueWatcher<Integer> {

        public TestData(String name) {
            setExtra(name);
            getState().addWatcher(this);
//            setStrategy(STRATEGY_APPEND_TAIL);
        }

        @Override
        public void onValueChange(Integer newValue, Integer oldValue) {
            Logger.getDefault().w("%s change state from %s to %s", this, oldValue, newValue);
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
