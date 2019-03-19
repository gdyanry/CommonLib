package yanry.lib.java.model.json.pattern;

public enum BaseType implements JsonType {
    Array, String, Number, Boolean;

    @Override
    public java.lang.String toJSONString() {
        return null;
    }
}
