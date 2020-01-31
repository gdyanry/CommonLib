package yanry.lib.java.model.json.pattern;

import java.util.HashMap;
import java.util.Objects;

import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.log.Logger;

public class JsonPattern extends HashMap<String, JsonType> implements JsonType {
    public static JsonPattern get(JSONObject jsonObject) {
        JsonPattern pattern = new JsonPattern();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (JSONObject.NULL.equals(value)) {
                pattern.put(key, BaseType.Null);
            } else if (value instanceof Number) {
                pattern.put(key, BaseType.Number);
            } else if (value instanceof String || value instanceof Character) {
                pattern.put(key, BaseType.String);
            } else if (value instanceof Boolean) {
                pattern.put(key, BaseType.Boolean);
            } else if (value instanceof JSONArray) {
                pattern.put(key, BaseType.Array);
            } else if (value instanceof JSONObject) {
                pattern.put(key, get((JSONObject) value));
            } else {
                Logger.getDefault().ee("no relative json type found for: ", value.getClass());
            }
        }
        return pattern;
    }

    public boolean matches(JSONObject jsonObject) {
        return isSubsetOf(get(jsonObject));
    }

    public boolean isSubsetOf(JsonPattern pattern) {
        for (Entry<String, JsonType> entry : entrySet()) {
            JsonType thatType = pattern.get(entry.getKey());
            if (thatType == null) {
                return false;
            }
            JsonType thisType = entry.getValue();
            if (Objects.equals(thisType, thatType) || thisType == BaseType.Null || thatType == BaseType.Null ||
                    thisType instanceof JsonPattern && thatType instanceof JsonPattern && ((JsonPattern) thisType).isSubsetOf((JsonPattern) thatType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public JsonPattern and(JsonPattern pattern) {
        JsonPattern result = new JsonPattern();
        for (Entry<String, JsonType> entry : entrySet()) {
            String key = entry.getKey();
            JsonType thatType = pattern.get(key);
            if (thatType != null) {
                JsonType thisType = entry.getValue();
                if (thisType.equals(thatType) || thatType == BaseType.Null) {
                    result.put(key, thisType);
                } else if (thisType == BaseType.Null) {
                    result.put(key, thatType);
                } else if (thisType instanceof JsonPattern && thatType instanceof JsonPattern) {
                    result.put(key, ((JsonPattern) thisType).and((JsonPattern) thatType));
                }
            }
        }
        return result;
    }

    @Override
    public final String toString() {
        return new JSONObject(this).toString();
    }

    @Override
    public String toJSONString() {
        return new JSONObject(this).toString();
    }
}
