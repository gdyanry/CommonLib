package lib.common.revert;

public interface Revertible {
    void proceed();

    void recover();
}
