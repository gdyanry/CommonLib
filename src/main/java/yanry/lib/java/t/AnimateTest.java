package yanry.lib.java.t;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.ValueAnimator;
import yanry.lib.java.model.log.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yanry on 2019/12/20.
 */
public class AnimateTest {
    public static void main(String[] args) {
        ValueAnimator valueAnimator = new ValueAnimator(10000);
        valueAnimator.addFlag(ValueAnimator.FLAG_REVERSE);
        int[] keyValues = {1, 10, 100};
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                Logger.getDefault().d("%s, %s, %s", valueAnimator.isFinish(), valueAnimator.getElapsedTime(), valueAnimator.getAnimatedValue(keyValues));
            }
        }, 0, 500);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                valueAnimator.seekTo(0.3f);
                valueAnimator.setPause(true);
            }
        }, 5000);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                valueAnimator.setPause(false);
            }
        }, 8000);
    }
}
