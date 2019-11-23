package yanry.lib.java.model.log;

import yanry.lib.java.model.cache.TimedObjectPool;

import java.util.List;

public class FormattedLog {
    private static Pool pool = new Pool();

    public static FormattedLog get(String log, int messageStart, int messageEnd) {
        FormattedLog formattedLog = pool.borrow();
        formattedLog.log = log;
        formattedLog.messageStart = messageStart;
        formattedLog.messageEnd = messageEnd;
        return formattedLog;
    }

    private String log;
    private int messageStart;
    private int messageEnd;

    private FormattedLog() {
    }

    void recycle() {
        pool.giveBack(this);
    }

    public String getLog() {
        return log;
    }

    public int getMessageStart() {
        return messageStart;
    }

    public int getMessageEnd() {
        return messageEnd;
    }

    private static class Pool extends TimedObjectPool<FormattedLog> {

        private Pool() {
            super(120);
        }

        @Override
        protected FormattedLog createInstance() {
            return new FormattedLog();
        }

        @Override
        protected void onReturn(FormattedLog obj) {
            obj.log = null;
            obj.messageStart = 0;
            obj.messageEnd = 0;
        }

        @Override
        protected void onClean(List<FormattedLog> discarded) {
        }
    }
}
