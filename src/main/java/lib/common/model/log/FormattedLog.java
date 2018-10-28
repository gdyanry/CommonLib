package lib.common.model.log;

import lib.common.model.cache.TimedObjectPool;

public class FormattedLog {
    private static Pool pool = new Pool();
    private String log;
    private int messageStart;
    private int messageEnd;

    static FormattedLog get(String log, int messageStart, int messageEnd) {
        FormattedLog formattedLog = pool.borrow();
        formattedLog.log = log;
        formattedLog.messageStart = messageStart;
        formattedLog.messageEnd = messageEnd;
        return formattedLog;
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
            super(LogRecord.TIMEOUT_SECOND);
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
        protected void onDiscard(FormattedLog obj) {
        }

        @Override
        protected void onCleared(int poolSize) {
        }
    }
}
