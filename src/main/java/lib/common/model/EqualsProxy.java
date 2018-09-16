package lib.common.model;

import java.util.Objects;
import java.util.function.Function;

public class EqualsProxy<T> {
    private T object;
    private Function<T, Object>[] concernedFields;

    public EqualsProxy(T object, Function<T, Object>... concernedFields) {
        this.object = object;
        this.concernedFields = concernedFields;
    }

    public int getHashCode() {
        Object[] fields = new Object[concernedFields.length];
        for (int i = 0; i < concernedFields.length; i++) {
            fields[i] = concernedFields[i].apply(object);
        }
        return Objects.hash(fields);
    }

    public boolean checkEquals(Object obj) {
        if (obj == object) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(object.getClass())) {
            return false;
        }
        T that = (T) obj;
        for (int i = 0; i < concernedFields.length; i++) {
            Function<T, Object> concernedField = concernedFields[i];
            if (!Objects.equals(concernedField.apply(object), concernedField.apply(that))) {
                return false;
            }
        }
        return true;
    }
}
