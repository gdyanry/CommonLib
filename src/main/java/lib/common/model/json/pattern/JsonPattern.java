package lib.common.model.json.pattern;

import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;
import lib.common.util.object.EqualsPart;
import lib.common.util.object.HandyObject;

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
