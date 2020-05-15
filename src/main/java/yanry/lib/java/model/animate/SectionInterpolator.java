package yanry.lib.java.model.animate;

/**
 * 区间插值器，将原来密度均匀的数值等分映射到给定区间中，导致各区间之间数值密度不相等。
 * <p>
 * Created by yanry on 2020/5/14.
 */
public class SectionInterpolator implements ProgressInterpolator {
    private float[] sections;

    /**
     * @param sections 区间分界点值，必须在0到1之间且按升序排列。
     */
    public SectionInterpolator(float... sections) {
        if (sections.length == 0) {
            throw new IllegalArgumentException("number of section must be > 0");
        }
        float previous = 0;
        for (float section : sections) {
            if (section < previous || section > 1) {
                throw new IllegalArgumentException("section values must be lined in ascending order and <= 1");
            }
        }
        this.sections = sections;
    }

    @Override
    public float getInterpolation(float input) {
        float lastSection = 0;
        int length = sections.length;
        for (int i = 0; i < length; i++) {
            float section = sections[i];
            if (input > section) {
                lastSection = section;
            } else if (input == section) {
                return 1f * (i + 1) / (length + 1);
            } else {
                return ValueAnimator.calculateValueByProportion(1f * i / (length + 1), 1f * (i + 1) / (length + 1), (input - lastSection) / (section - lastSection));
            }
        }
        return ValueAnimator.calculateValueByProportion(1f * length / (length + 1), 1, (input - lastSection) / (1 - lastSection));
    }
}
