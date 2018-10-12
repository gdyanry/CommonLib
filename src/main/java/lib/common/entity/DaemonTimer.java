package lib.common.entity;

import java.util.Timer;

public class DaemonTimer extends Timer {
    public DaemonTimer() {
        super(true);
    }
}
