package yanry.lib.java.util;

import yanry.lib.java.model.log.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * @author yanry
 * <p>
 * 2016年2月5日
 */
public class ReflectionUtil {

    /**
     * Initialize all "public static String" fields's value as the field names.
     * 此方法可以在目标类的静态代码块中调用并且不受代码混淆影响。
     *
     * @param c
     */
    public static void initStaticStringFields(Class<?> c) {
        for (Field f : c.getFields()) {
            if (f.getType().equals(String.class) && Modifier.isPublic(f.getModifiers())
                    && Modifier.isStatic(f.getModifiers())) {
                try {
                    f.set(null, f.getName());
                } catch (Exception e) {
                    Logger.getDefault().catches(e);
                }
            }
        }
    }

    public static ArrayList<String> getStaticStringFieldNames(Class<?> c) {
        ArrayList<String> list = new ArrayList<>();
        for (Field f : c.getFields()) {
            if (f.getType().equals(String.class) && Modifier.isPublic(f.getModifiers())
                    && Modifier.isStatic(f.getModifiers())) {
                list.add(f.getName());
            }
        }
        return list;
    }
}
