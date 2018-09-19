package lib.common.util.object;

import lib.common.model.log.Logger;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

public class ObjectUtil {
    public static int hashCode(Object object) {
        Class<?> type = object.getClass();
        return Objects.hash(Stream.of(type.getMethods()).filter(method -> method.isAnnotationPresent(HashAndEquals.class))
                .map(method -> {
                    try {
                        return method.invoke(object);
                    } catch (ReflectiveOperationException e) {
                        Logger.getDefault().catches(e);
                        return null;
                    }
                }).toArray());
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
}

