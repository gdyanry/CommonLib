package yanry.lib.java.model.json.pattern;

import java.util.Objects;

import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.json.JSONString;

import static yanry.lib.java.model.json.pattern.JsonElement.Null;

interface JsonPattern extends JSONString {

    static boolean acceptValue(Object patternValue, Object jsonValue) {
        if (Objects.equals(patternValue, jsonValue)) {
            return true;
        }
        if (patternValue instanceof JsonPattern) {
            JsonPattern pattern = (JsonPattern) patternValue;
            if (pattern.acceptPatternOrValue(jsonValue)) {
                return true;
            }
        }
        return false;
    }

    default boolean acceptPatternOrValue(Object patternOrValue) {
        return patternOrValue == this || patternOrValue == Null || JSONObject.NULL.equals(patternOrValue) || acceptValue(patternOrValue);
    }

    boolean acceptValue(Object value);
}
