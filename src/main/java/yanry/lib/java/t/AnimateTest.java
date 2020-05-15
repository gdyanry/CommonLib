package yanry.lib.java.t;

import java.util.Timer;
import java.util.TimerTask;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.animate.ProportionTransformer;
import yanry.lib.java.model.animate.SectionTransformer;
import yanry.lib.java.model.animate.TimeController;
import yanry.lib.java.model.animate.ValueAnimator;
import yanry.lib.java.model.log.Logger;

/**
 * Created by yanry on 2019/12/20.
 */
public class AnimateTest {
    public static void main(String[] args) {
        TimeController timeController = new TimeController();
        ValueAnimator valueAnimator = new ValueAnimator(4000, 100, 90, 100);
        ProportionTransformer transformer = new SectionTransformer(0.1f, 0.8f);
        valueAnimator.setProportionTransformer(transformer);
        valueAnimator.setRepeatCount(1);
        valueAnimator.addFlag(ValueAnimator.FLAG_REVERSE);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = timeController.getElapsedTime();
                Logger.getDefault().d("%s, %s, %s", timeController.isPause(), elapsedTime, valueAnimator.getAnimatedValue(elapsedTime));
                if (valueAnimator.isFinish(elapsedTime)) {
                    System.exit(0);
                }
            }
        }, 0, 500);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
//                valueAnimator.seekTo(0.3f);
                timeController.setPause(true);
            }
        }, 2000);
        Singletons.get(Timer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                timeController.setPause(false);
            }
        }, 3000);
    }
}
