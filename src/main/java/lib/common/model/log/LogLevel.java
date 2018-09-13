package lib.common.model.log;

public enum LogLevel {
    Verbase, Debug, Info, Warn, Error;

    private String acronym;

    LogLevel() {
        acronym = String.valueOf(name().charAt(0));
    }

    public String getAcronym() {
        return acronym;
    }

    boolean test(LogLevel levelToTest) {
        return ordinal() <= levelToTest.ordinal();
    }
}
