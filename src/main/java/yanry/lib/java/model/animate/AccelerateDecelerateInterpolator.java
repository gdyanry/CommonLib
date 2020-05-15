package yanry.lib.java.model.animate;

/**
 * 基于三角函数的动画插值器，可实现【慢-快-慢】的动画效果。
 * <p>
 * Created by yanry on 2019/12/21.
 */
public class AccelerateDecelerateInterpolator implements ProgressInterpolator {

    @Override
    public float getInterpolation(float input) {
        return (float) (Math.cos(Math.PI * (1 + input)) / 2 + 0.5);
    }
}
