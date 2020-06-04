package yanry.lib.java.model.json.pattern;

import java.util.ArrayList;

import yanry.lib.java.model.json.JSONArray;

/**
 * Created by yanry on 2020/2/2.
 */
public class JsonArrayPattern extends ArrayList<Object> implements JsonPattern {

    public static JsonArrayPattern get(JSONArray jsonArray, int depth) {
        JsonArrayPattern arrayPattern = new JsonArrayPattern(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            arrayPattern.add(JsonElement.getPattern(jsonArray.opt(i), depth - 1));
        }
        return arrayPattern;
    }

    public JsonArrayPattern(int i) {
        super(i);
    }

    public JsonArrayPattern() {
    }

    @Override
    public boolean acceptValue(Object value) {
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            int arrayLen = jsonArray.length();
            for (int i = 0; i < size(); i++) {
                if (arrayLen > i) {
                    Object patternValue = get(i);
                    if (!JsonPattern.acceptValue(patternValue, jsonArray.opt(i))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toJSONString() {
        return new JSONArray(this).toString();
    }
}
