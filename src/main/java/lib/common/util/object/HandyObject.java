package lib.common.util.object;

@Presentable
public class HandyObject {
    @Override
    public final int hashCode() {
        return ObjectUtil.hashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return ObjectUtil.equals(this, obj);
    }

    @Override
    public String toString() {
        return ObjectUtil.getPresentation(this).toString();
    }
}
