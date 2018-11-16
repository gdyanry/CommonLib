package lib.common.util.object;

import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;
import lib.common.util.StringUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class ObjectUtil {
    public static int hashCode(Object object) {
        if (object == null) {
            return 0;
        }
        Class<?> type = object.getClass();
        ArrayList<Object> fields = new ArrayList<>();
        fields.add(type);
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(EqualsPart.class)) {
                try {
                    fields.add(method.invoke(object));
                } catch (ReflectiveOperationException e) {
                    Logger.getDefault().catches(e);
                }
            }
        }
        return Objects.hash(fields.toArray());
    }

    public static boolean equals(Object object, Object that) {
        if (that == object) {
            return true;
        }
        if (object == null || that == null) {
            return false;
        }
        Class<?> type = object.getClass();
        if (!that.getClass().equals(type)) {
            return false;
        }
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(EqualsPart.class)) {
                try {
                    if (!checkEquals(method.invoke(object), method.invoke(that))) {
                        return false;
                    }
                } catch (ReflectiveOperationException e) {
                    Logger.getDefault().catches(e);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkEquals(Object value1, Object value2) {
        if (value1 == value2) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        if (value1.getClass().isArray() && value2.getClass().isArray()) {
            int length = Array.getLength(value1);
            if (length == Array.getLength(value2)) {
                for (int i = 0; i < length; i++) {
                    if (!checkEquals(Array.get(value1, i), Array.get(value2, i))) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return Objects.equals(value1, value2);
    }

    /**
     * @param object
     * @return the return object is one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONObject.NULL object.
     */
    public static Object getPresentation(Object object) {
        if (object == null) {
            return JSONObject.NULL;
        }
        Class<?> type = object.getClass();
        String typeSymbol = "@";
        if (type.isAnnotationPresent(Visible.class)) {
            JSONObject jsonObject = new JSONObject().put(typeSymbol, StringUtil.getClassName(object));
            for (Method method : type.getMethods()) {
                if (method.isAnnotationPresent(Visible.class)) {
                    String key = StringUtil.setFirstLetterCase(method.getName().replaceFirst("^get", ""), false);
                    try {
                        jsonObject.put(key, getPresentation(method.invoke(object)));
                    } catch (ReflectiveOperationException e) {
                        Logger.getDefault().catches(e);
                    }
                }
            }
            return jsonObject;
        }
        if (object instanceof Enum) {
            return ((Enum) object).name();
        }
        if (object.getClass().isArray()) {
            JSONArray jsonArray = new JSONArray();
            int len = Array.getLength(object);
            for (int i = 0; i < len; i++) {
                jsonArray.put(getPresentation(Array.get(object, i)));
            }
            return jsonArray;
        } else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            Collection list = (Collection) object;
            for (Object item : list) {
                jsonArray.put(getPresentation(item));
            }
            return jsonArray;
        } else if (object instanceof Map) {
            JSONArray jsonArray = new JSONArray();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                jsonArray.put(new JSONArray().put(getPresentation(key)).put(getPresentation(map.get(key))));
            }
            return jsonArray;
        }
        return object;
    }
}

