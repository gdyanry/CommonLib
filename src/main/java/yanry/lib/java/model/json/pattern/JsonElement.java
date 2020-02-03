package yanry.lib.java.model.json.pattern;

import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.log.Logger;

public enum JsonElement implements JsonPattern {
    /**
     * Null在JsonPattern中代表任意类型或值。
     */
    Null {
        @Override
        public boolean acceptValue(Object value) {
            return true;
        }
    },
    String {
        @Override
        public boolean acceptValue(Object value) {
            return value instanceof String;
        }
    },
    Number {
        @Override
        public boolean acceptValue(Object value) {
            return value instanceof Number;
        }
    },
    Boolean {
        @Override
        public boolean acceptValue(Object value) {
            return value instanceof Boolean;
        }
    },
    JsonObject {
        @Override
        public boolean acceptValue(Object value) {
            return value instanceof JsonObjectPattern || value instanceof JSONArray;
        }
    },
    JsonArray {
        @Override
        public boolean acceptValue(Object value) {
            return value instanceof JsonArrayPattern || value instanceof JSONObject;
        }
    };

    static JsonPattern getPattern(Object jsonElement, int depth) {
        if (JSONObject.NULL.equals(jsonElement)) {
            return Null;
        }
        if (jsonElement instanceof String) {
            return String;
        }
        if (jsonElement instanceof Number) {
            return Number;
        }
        if (jsonElement instanceof Boolean) {
            return Boolean;
        }
        if (jsonElement instanceof JSONObject) {
            if (depth <= 0) {
                return JsonObject;
            }
            return JsonObjectPattern.get((JSONObject) jsonElement, depth);
        }
        if (jsonElement instanceof JSONArray) {
            if (depth <= 0) {
                return JsonArray;
            }
            return JsonArrayPattern.get((JSONArray) jsonElement, depth);
        }
        Logger.getDefault().ee("no relative json type found for: ", jsonElement.getClass());
        return Null;
    }

    private String jsonString;

    JsonElement() {
        jsonString = new StringBuilder().append('"').append('<').append(name()).append('>').append('"').toString();
    }

    @Override
    public String toJSONString() {
        return jsonString;
    }
}
