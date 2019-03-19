package lib.common.util.object;

import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;
import lib.common.util.HexUtil;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

public class ObjectUtil {

    public static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass().isArray() && b.getClass().isArray()) {
            int length = Array.getLength(a);
            if (length == Array.getLength(b)) {
                for (int i = 0; i < length; i++) {
                    if (!equals(Array.get(a, i), Array.get(b, i))) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return a.equals(b);
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
            JSONObject jsonObject = new JSONObject().put(typeSymbol, StringUtil.getSimpleClassName(object));
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

    public static byte[] getSnapshot(Object object) throws IOException {
        if (!(object instanceof Serializable)) {
            object = object.toString();
        }
        return IOUtil.object2Bytes(object);
    }

    public static String getSnapshotMd5(Object object) throws IOException, NoSuchAlgorithmException {
        return HexUtil.bytesToHex(MessageDigest.getInstance("MD5").digest(getSnapshot(object)));
    }
}

