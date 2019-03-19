package yanry.lib.java.model.json.pattern;

import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.object.EqualsPart;
import yanry.lib.java.util.object.HandyObject;
import yanry.lib.java.util.object.Visible;

import java.util.HashMap;

public class JsonPattern extends HandyObject implements JsonType {
    private HashMap<String, Object> map;

    private JsonPattern() {
        map = new HashMap<>();
    }

    public static JsonPattern get(JSONObject jsonObject) {
        JsonPattern pattern = new JsonPattern();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (JSONObject.NULL.equals(value)) {
                pattern.map.put(key, NullType.get());
            } else if (value instanceof Number) {
                pattern.map.put(key, BaseType.Number);
            } else if (value instanceof String) {
                pattern.map.put(key, BaseType.String);
            } else if (value instanceof Boolean) {
                pattern.map.put(key, BaseType.Boolean);
            } else if (value instanceof JSONArray) {
                pattern.map.put(key, BaseType.Array);
            } else if (value instanceof JSONObject) {
                pattern.map.put(key, get((JSONObject) value));
            } else {
                Logger.getDefault().ee("no relative json type found for: ", value.getClass());
            }
        }
        return pattern;
    }

    public boolean matches(JSONObject jsonObject) {
        return equals(get(jsonObject));
    }

    public JsonPattern and(JsonPattern pattern) {
        JsonPattern result = new JsonPattern();
        for (String key : map.keySet()) {
            Object val2 = pattern.map.get(key);
            if (val2 != null) {
                Object val1 = map.get(key);
                if (val1.equals(val2)) {
                    result.map.put(key, val1);
                } else if (val1 instanceof JsonPattern && val2 instanceof JsonPattern) {
                    JsonPattern pattern1 = (JsonPattern) val1;
                    JsonPattern pattern2 = (JsonPattern) val2;
                    result.map.put(key, pattern1.and(pattern2));
                }
            }
        }
        return result;
    }

    @Visible
    @EqualsPart
    public HashMap<String, Object> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return new JSONObject(map).toString();
    }

    @Override
    public String toJSONString() {
        return new JSONObject(map).toString();
    }
}
