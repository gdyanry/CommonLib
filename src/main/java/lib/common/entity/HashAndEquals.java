package lib.common.entity;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @param <T> type of the exact class that extends this class.
 */
public abstract class HashAndEquals<T extends HashAndEquals<T>> {
    private Object[] hashFields;

    protected abstract void addHashFields(ArrayList<Object> hashFields);

    protected abstract boolean equalsWithSameClass(T object);

    @Override
    public final int hashCode() {
        if (hashFields == null) {
            ArrayList list = new ArrayList();
            addHashFields(list);
            hashFields = list.toArray();
        }
        return Objects.hash(hashFields);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }
        return equalsWithSameClass((T) obj);
    }
}
