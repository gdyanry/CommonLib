package lib.common.model.revert;

public class RevertibleBoolean {
    private RevertManager manager;
    private boolean value;
    private SetBoolean setTrue;
    private SetBoolean setFalse;

    public RevertibleBoolean(RevertManager manager) {
        this.manager = manager;
        setTrue = new SetBoolean(true);
        setFalse = new SetBoolean(false);
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        if (this.value != value) {
            manager.proceed(value ? setTrue : setFalse);
        }
    }

    private class SetBoolean implements Revertible {
        private boolean valueToSet;

        public SetBoolean(boolean valueToSet) {
            this.valueToSet = valueToSet;
        }

        @Override
        public void proceed() {
            value = valueToSet;
        }

        @Override
        public void recover() {
            value = !valueToSet;
        }
    }
}
