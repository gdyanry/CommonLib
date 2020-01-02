package yanry.lib.java.t;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.schedule.Scheduler;
import yanry.lib.java.model.schedule.SchedulerManager;
import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.extend.BufferedStringDisplay;
import yanry.lib.java.model.schedule.imple.TimerScheduleRunner;

/**
 * Created by yanry on 2020/1/1.
 */
public class BufferedStringTest extends BufferedStringDisplay {

    public BufferedStringTest() {
        super(5, "#", System.lineSeparator(), true);
    }

    public static void main(String[] args) {
        SchedulerManager manager = new SchedulerManager(new TimerScheduleRunner("buf", false), Logger.getDefault());
        Scheduler scheduler = manager.get("buf");
        scheduler.show(new ShowData().setExtra("aaaaa"), BufferedStringTest.class);
        scheduler.show(new ShowData().setExtra("bbbbbb").setDuration(3000), BufferedStringTest.class);
    }

    @Override
    protected void onFlush(String segment) {
        System.out.println(segment);
    }
}
