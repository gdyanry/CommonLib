package yanry.lib.java.entity;

import java.util.Timer;

public class DaemonTimer extends Timer {
    public DaemonTimer() {
        super("DaemonTimer", true);
    }
}
