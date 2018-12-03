package lib.common.util.console.query;

import java.util.Scanner;

public abstract class ConsoleQuery<T> {

    public T getValue(String hint) {
        StringBuilder promptBuilder;
        if (hint != null && hint.length() > 0) {
            promptBuilder = new StringBuilder(hint).append(": ");
        } else {
            promptBuilder = new StringBuilder();
        }
        appendPromptInfo(promptBuilder);
        String prompt = promptBuilder.toString();
        Scanner scanner = new Scanner(System.in);
        String input;
        while (!isValid(input = readInput(scanner, prompt))) {
        }
        return map(input);
    }

    private String readInput(Scanner scanner, String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    protected abstract void appendPromptInfo(StringBuilder promptBuilder);

    protected abstract boolean isValid(String input);

    protected abstract T map(String input);
}
