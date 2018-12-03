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
        for (Map.Entry<String, T> entry : mapping.entrySet()) {
            promptBuilder.append(entry.getKey()).append('(').append(entry.getValue()).append(')');
        }
        if (mapping.size() > 0) {
            promptBuilder.deleteCharAt(promptBuilder.length() - 1);
        }
    }

    @Override
    protected boolean isValid(String input) {
        return mapping.containsKey(input);
    }

    @Override
    protected T map(String input) {
        return mapping.get(input);
    }
}
