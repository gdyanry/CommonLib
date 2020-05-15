package yanry.lib.java.model.animate;

/**
 * 时间控制管理器。用于控制时间的暂停、继续及跳转。
 * <p>
 * Created by yanry on 2020/5/14.
 */
public class TimeController {
    private long startTime;
    private long pauseTime;

    public TimeController() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 获取当前流逝的时间。
     *
     * @return
     */
    public long getElapsedTime() {
        return pauseTime > 0 ? pauseTime - startTime : System.currentTimeMillis() - startTime;
    }

    /**
     * 查询当前时间是否处于暂停的状态。
     *
     * @return
     */
    public boolean isPause() {
        return pauseTime > 0;
    }

    /**
     * 控制时间的暂停和继续。
     *
     * @param pause
     */
    public void setPause(boolean pause) {
        if (pause) {
            pauseTime = System.currentTimeMillis();
        } else if (pauseTime > 0) {
            startTime += System.currentTimeMillis() - pauseTime;
            pauseTime = 0;
        }
    }

    /**
     * 手动调节时间进度。
     *
     * @param timeOffset 相对于开始时间的时间偏移量。
     */
    public void seekTo(long timeOffset) {
        long now = System.currentTimeMillis();
        startTime = now - timeOffset;
        if (pauseTime > 0) {
            pauseTime = now;
        }
    }
}
