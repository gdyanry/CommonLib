package lib.common.util.console.query;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptionsMap<T> extends ConsoleQuery<T> {
    private LinkedHashMap<String, T> mapping;

    public OptionsMap() {
        mapping = new LinkedHashMap<>();
    }

    public OptionsMap<T> appendMapping(String read, T value) {
        mapping.put(read, value);
        return this;
    }

    @Override
    protected void appendPromptInfo(StringBuilder promptBuilder) {
        boolean isFirst = true;
        for (Map.Entry<String, T> entry : mapping.entrySet()) {
            promptBuilder.append(entry.getKey()).append('(').append(entry.getValue()).append(')');
            if (!isFirst) {
                promptBuilder.append('/');
            } else {
                isFirst = false;
            }
        }
    }

    @Override
    protected boolean isValid(String input) {
        return mapping.containsValue(input);
    }

    @Override
    protected T map(String input) {
        return mapping.get(input);
    }
}
