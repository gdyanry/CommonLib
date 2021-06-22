package yanry.lib.java.entity;

import java.util.Timer;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

public class DaemonTimer extends Timer {
    public DaemonTimer() {
        super("DaemonTimer", true);
    }

    @Override
    public int purge() {
        int purge = super.purge();
        Logger.getDefault().concat(LogLevel.Debug, "daemon timer purge: ", purge);
        return purge;
    }
}
