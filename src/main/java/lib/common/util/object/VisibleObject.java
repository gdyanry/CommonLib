package lib.common.util.object;

@Visible
public class VisibleObject {
    @Override
    public String toString() {
        return ObjectUtil.getPresentation(this).toString();
    }
}
