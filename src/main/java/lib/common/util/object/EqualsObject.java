package lib.common.util.object;

public abstract class EqualsObject {
    @Override
    public final int hashCode() {
        return ObjectUtil.hashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return ObjectUtil.equals(this, obj);
    }
}
