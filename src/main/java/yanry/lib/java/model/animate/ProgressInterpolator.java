package yanry.lib.java.model.animate;

/**
 * 进度比例转换接口。
 */
public interface ProgressInterpolator {
    /**
     * @param input 输入值，取值范围(0, 1)
     * @return 变换值，取值范围为任意浮点数
     */
    float getInterpolation(float input);
}
