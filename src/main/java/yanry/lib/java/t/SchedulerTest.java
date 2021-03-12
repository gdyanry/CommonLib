package yanry.lib.java.t;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.model.runner.Runner;
import yanry.lib.java.model.runner.RunnerBlockMonitor;
import yanry.lib.java.model.runner.TimerRunner;
import yanry.lib.java.model.schedule.Scheduler;
import yanry.lib.java.model.schedule.SchedulerManager;
import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.imple.ReusableDisplay;
import yanry.lib.java.model.watch.ValueWatcher;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yanry on 2019/12/17.
 */
public class SchedulerTest {
    public static void main(String[] args) {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.TIME).addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.METHOD)
                .addFlag(SimpleFormatter.THREAD).addFlag(SimpleFormatter.LEVEL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        Logger.getDefault().addHandler(handler);

        testSchedule();
//        testMonitor();
    }

    private static void testSchedule() {
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
        TestData enqueueData = new TestData("ENQUEUE");
        enqueueData.setStrategy(ShowData.STRATEGY_APPEND_TAIL).setDuration(5000);
        scheduler.show(enqueueData, TestDisplay.class);
        scheduler.show(enqueueData, TestDisplay.class);
    }

    private static void testMonitor() {
        RunnerBlockMonitor monitor = new RunnerBlockMonitor(new TimerRunner("monitoring", false)) {
            @Override
            protected void onAckTimeout(Runner monitoredRunner) {

            }
        };
        Logger.getDefault().dd("start monitor.");
        TimerRunner monitored = new TimerRunner("monitored", false);
        monitor.start(monitored, 600, 1000, 0);
        monitored.schedule(new TimerTask() {
            private long sleep = 100;

            @Override
            public void run() {
                try {
                    sleep += 100;
                    Logger.getDefault().dd("sleep: ", sleep);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
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
