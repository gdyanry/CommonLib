package lib.common.model.revert;

public interface Revertible {
    void proceed();

    void recover();
}
