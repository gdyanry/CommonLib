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
        Class<?> type = object.getClass();
        ArrayList<Object> fields = new ArrayList<>();
        fields.add(type);
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(HashAndEquals.class)) {
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
        Class<?> type = object.getClass();
        if (that == null || !that.getClass().equals(type)) {
            return false;
        }
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(HashAndEquals.class)) {
                try {
                    if (!Objects.equals(method.invoke(object), method.invoke(that))) {
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

    /**
     * @param object
     * @param typeSymbol
     * @return the return object is one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONObject.NULL object.
     */
    public static Object getPresentation(Object object, String typeSymbol) {
        if (object == null) {
            return null;
        }
        Class<?> type = object.getClass();
        if (type.isAnnotationPresent(Presentable.class)) {
            JSONObject jsonObject = new JSONObject().put(typeSymbol, StringUtil.getClassName(object));
            for (Method method : type.getMethods()) {
                if (method.isAnnotationPresent(Presentable.class)) {
                    String key = StringUtil.setFirstLetterCase(method.getName().replaceFirst("^get", ""), false);
                    try {
                        Object value = method.invoke(object);
                        if (value != null) {
                            jsonObject.put(key, getPresentation(value, typeSymbol));
                        }
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
                jsonArray.put(getPresentation(Array.get(object, i), typeSymbol));
            }
            return jsonArray;
        } else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            Collection list = (Collection) object;
            for (Object item : list) {
                jsonArray.put(getPresentation(item, typeSymbol));
            }
            return jsonArray;
        } else if (object instanceof Map) {
            JSONArray jsonArray = new JSONArray();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                jsonArray.put(new JSONArray().put(getPresentation(key, typeSymbol)).put(getPresentation(map.get(key), typeSymbol)));
            }
            return jsonArray;
        }
        return object;
    }
}

