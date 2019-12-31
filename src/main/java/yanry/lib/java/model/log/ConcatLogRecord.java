package yanry.lib.java.model.log;

public class ConcatLogRecord extends LogRecord {
    private Object[] msgParts;

    public ConcatLogRecord(Object tag, LogLevel level, int encapsulationLayerCount, boolean anonymous, Object[] msgParts) {
        super(tag, level, encapsulationLayerCount, anonymous);
        this.msgParts = msgParts;
    }

    @Override
    protected String buildMessage() {
        if (msgParts == null || msgParts.length == 0) {
            return "";
        }
        if (msgParts.length == 1) {
            return msgParts[0].toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object part : msgParts) {
            stringBuilder.append(part);
        }
        return stringBuilder.toString();
    }
}
