package lib.common.model.revert;

import java.util.Objects;

public class RevertibleObject<T> {
    private RevertManager manager;
    private T value;

    public RevertibleObject(RevertManager manager) {
        this.manager = manager;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (!Objects.equals(this.value, value)) {
            manager.proceed(new SetValue(value));
        }
    }

    private class SetValue implements Revertible {
        private T copy;
        private T valueToSet;

        public SetValue(T valueToSet) {
            this.valueToSet = valueToSet;
            copy = value;
        }

        @Override
        public void proceed() {
            value = valueToSet;
        }

        @Override
        public void recover() {
            value = copy;
        }
    }
}
