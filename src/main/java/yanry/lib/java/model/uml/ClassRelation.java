package yanry.lib.java.model.uml;

public enum ClassRelation {
    Extends("--|>"),
    Implements("..|>"),
    /**
     * Composition
     */
    Contains("*-->"),
    /**
     * Aggregation
     */
    Has("o-->"),
    /**
     * Dependency
     */
    Uses("..>"),
    /**
     * Association
     */
    Navigate("-->");

    private String symbol;

    ClassRelation(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
