package lib.common.interfaces;

public interface Loggable {
    void debug(String msg, Object... args);

    void error(String msg, Object... args);
}
