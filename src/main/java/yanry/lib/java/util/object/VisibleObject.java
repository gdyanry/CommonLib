package yanry.lib.java.util.object;

@Visible
public class VisibleObject {
    @Override
    public String toString() {
        return ObjectUtil.getPresentation(this).toString();
    }
}
