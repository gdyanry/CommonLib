package lib.common.util.object;

@Presentable
public abstract class EqualsObject {
    @HashAndEquals
    @Override
    public final int hashCode() {
        return ObjectUtil.hashCode(this);
    }

    @HashAndEquals
    @Override
    public final boolean equals(Object obj) {
        return ObjectUtil.equals(this, obj);
    }
}
