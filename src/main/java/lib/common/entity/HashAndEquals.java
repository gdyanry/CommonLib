package lib.common.entity;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @param <T> type of the exact class that extends this class.
 */
public class HashAndEquals<T extends HashAndEquals<T>> {
    private Function<T, Object>[] concernedFields;
    private Object[] fields;

    public HashAndEquals(Function<T, Object>... concernedFields) {
        this.concernedFields = concernedFields;
        fields = new Object[concernedFields.length];
        for (int i = 0; i < concernedFields.length; i++) {
            Function<T, Object> concernedField = concernedFields[i];
            fields[i] = concernedField.apply((T) this);
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }
        T that = (T) obj;
        for (int i = 0; i < concernedFields.length; i++) {
            Function<T, Object> concernedField = concernedFields[i];
            Object thatField = concernedField.apply(that);
            if (!Objects.equals(fields[i], thatField)) {
                return false;
            }
        }
        return true;
    }
}
