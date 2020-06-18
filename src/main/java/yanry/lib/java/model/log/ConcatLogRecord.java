package yanry.lib.java.model.log;

import java.util.Objects;

public class ConcatLogRecord extends LogRecord {
    private Object[] msgParts;

    public ConcatLogRecord(Object tag, LogLevel level, int encapsulationLayerCount, Object[] msgParts) {
        super(tag, level, encapsulationLayerCount);
        this.msgParts = msgParts;
    }

    @Override
    protected String buildMessage() {
        if (msgParts == null || msgParts.length == 0) {
            return "";
        }
        if (msgParts.length == 1) {
            return Objects.toString(msgParts[0]);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object part : msgParts) {
            stringBuilder.append(part);
        }
        return stringBuilder.toString();
    }
}
