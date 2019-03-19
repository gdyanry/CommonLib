package yanry.lib.java.model.json.pattern;

public class NullType implements JsonType {
    private static volatile NullType instance;

    private NullType() {
    }

    public static NullType get() {
        if (instance == null) {
            synchronized (NullType.class) {
                if (instance == null) {
                    instance = new NullType();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof JsonType;
    }

    @Override
    public String toJSONString() {
        return null;
    }

    @Override
    public String toString() {
        return "Null";
    }
}
