/**
 * 
 */
package lib.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author yanry
 *
 *         2016年2月5日
 */
public class ReflectionUtil {

	/**
	 * Initialize all "public static String" fields's value as the field names.
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
					e.printStackTrace();
				}
			}
		}
	}
}
