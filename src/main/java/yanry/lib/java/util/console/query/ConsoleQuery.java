package yanry.lib.java.util.console.query;

import java.util.Scanner;

public abstract class ConsoleQuery<T> {
    private Scanner scanner;

    public ConsoleQuery() {
        scanner = new Scanner(System.in);
    }

    public T getValue(String hint) {
        StringBuilder promptBuilder;
        if (hint != null && hint.length() > 0) {
            promptBuilder = new StringBuilder(hint).append(": ");
        } else {
            promptBuilder = new StringBuilder();
        }
        appendPromptInfo(promptBuilder);
        String prompt = promptBuilder.toString();
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            if (isValid(input)) {
                return map(input);
            }
        }
    }

    public void close() {
        scanner.close();
    }

    protected abstract void appendPromptInfo(StringBuilder promptBuilder);

    protected abstract boolean isValid(String input);

    protected abstract T map(String input);
}
