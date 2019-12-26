package yanry.lib.java.t;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.animate.AccelerateDecelerateInterpolator;
import yanry.lib.java.model.animate.TimeInterpolator;
import yanry.lib.java.model.animate.ValueAnimator;
import yanry.lib.java.model.log.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yanry on 2019/12/20.
 */
public class AnimateTest {
    public static void main(String[] args) {
        ValueAnimator valueAnimator = new ValueAnimator(5000);
        TimeInterpolator powerInterpolator = new AccelerateDecelerateInterpolator();
        valueAnimator.setRepeatCount(3);
        valueAnimator.addFlag(ValueAnimator.FLAG_REVERSE);
        float[] keyValues = {0, 100};
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                Logger.getDefault().d("%s, %s, %s", valueAnimator.isPause(), valueAnimator.getElapsedTime(), valueAnimator.getAnimatedValue(keyValues, powerInterpolator));
                if (valueAnimator.isFinish()) {
                    System.exit(0);
                }
            }
        }, 0, 500);
//        Singletons.get(Timer.class).schedule(new TimerTask() {
//            @Override
//            public void run() {
//                valueAnimator.seekTo(0.3f);
//                valueAnimator.setPause(true);
//            }
//        }, 5000);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                valueAnimator.setPause(false);
            }
        }, 6000);
    }
}
