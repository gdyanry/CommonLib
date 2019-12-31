package yanry.lib.java.model.log;

class FormatLogRecord extends LogRecord {
    private String format;
    private Object[] args;

    public FormatLogRecord(Object tag, LogLevel level, int encapsulationLayerCount, boolean anonymous, String format, Object[] args) {
        super(tag, level, encapsulationLayerCount, anonymous);
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
