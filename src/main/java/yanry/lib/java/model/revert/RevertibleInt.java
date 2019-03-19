package yanry.lib.java.model.revert;

public class RevertibleInt {
    private RevertManager manager;
    private int value;

    public RevertibleInt(RevertManager manager) {
        this.manager = manager;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        if (this.value != value) {
            manager.proceed(new SetLong(value));
        }
    }

    public void increment() {
        set(value + 1);
    }

    private class SetLong implements Revertible {
        private int copy;
        private int valueToSet;

        public SetLong(int valueToSet) {
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
