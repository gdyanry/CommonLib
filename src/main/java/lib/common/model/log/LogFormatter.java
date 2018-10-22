package lib.common.model.log;

public interface LogFormatter {
    FormattedLog format(LogRecord logRecord);
}
