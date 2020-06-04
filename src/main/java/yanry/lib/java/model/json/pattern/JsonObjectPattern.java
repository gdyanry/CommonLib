package yanry.lib.java.model.json.pattern;

import java.util.HashMap;

import yanry.lib.java.model.json.JSONObject;

public class JsonObjectPattern extends HashMap<String, Object> implements JsonPattern {
    public static JsonObjectPattern get(JSONObject jsonObject, int depth) {
        JsonObjectPattern objectPattern = new JsonObjectPattern();
        for (String key : jsonObject.keySet()) {
            objectPattern.put(key, JsonElement.getPattern(jsonObject.opt(key), depth - 1));
        }
        return objectPattern;
    }

    public JsonObjectPattern and(JSONObject jsonObject) {
        JsonObjectPattern result = new JsonObjectPattern();
        for (Entry<String, Object> entry : entrySet()) {
            String key = entry.getKey();
            Object patternValue = entry.getValue();
            Object jsonValue = jsonObject.opt(key);
            if (JsonPattern.acceptValue(patternValue, jsonValue)) {
                result.put(key, patternValue);
            } else if (patternValue instanceof JsonObjectPattern && jsonValue instanceof JSONObject) {
                result.put(key, ((JsonObjectPattern) patternValue).and((JSONObject) jsonValue));
            }
        }
        return result;
    }

    @Override
    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    @Override
    public boolean acceptValue(Object value) {
        if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            for (Entry<String, Object> entry : entrySet()) {
                String key = entry.getKey();
                Object patternValue = entry.getValue();
                Object jsonValue = jsonObject.opt(key);
                if (!JsonPattern.acceptValue(patternValue, jsonValue)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
