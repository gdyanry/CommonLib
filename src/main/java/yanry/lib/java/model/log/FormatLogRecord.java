package yanry.lib.java.model.log;

class FormatLogRecord extends LogRecord {
    private String format;
    private Object[] args;

    public FormatLogRecord(Object tag, LogLevel level, int encapsulationLayerCount, String format, Object[] args) {
        super(tag, level, encapsulationLayerCount);
        this.format = format;
        this.args = args;
    }

    @Override
    protected String buildMessage() {
        if (args == null || args.length == 0) {
            return format;
        }
        return String.format(format, args);
    }
}
