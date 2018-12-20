package lib.common.util.object;

import lib.common.model.log.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public class HandyObject extends VisibleObject implements Externalizable {
    @Override
    public final int hashCode() {
        Class<?> type = getClass();
        ArrayList<Object> fieldsToHash = new ArrayList<>();
        fieldsToHash.add(type);
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(EqualsPart.class)) {
                try {
                    fieldsToHash.add(method.invoke(this));
                } catch (ReflectiveOperationException e) {
                    Logger.getDefault().catches(e);
                }
            }
        }
        return Objects.hash(fieldsToHash.toArray());
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        Class<?> type = getClass();
        if (!obj.getClass().equals(type)) {
            return false;
        }
        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(EqualsPart.class)) {
                try {
                    if (!ObjectUtil.equals(method.invoke(this), method.invoke(obj))) {
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(EqualsPart.class)) {
                try {
                    Object invoke = method.invoke(this);
                    if (invoke instanceof Serializable) {
                        out.writeObject(out);
                    } else {
                        out.writeObject(invoke.getClass());
                    }
                } catch (ReflectiveOperationException e) {
                    Logger.getDefault().catches(e);
                }
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
