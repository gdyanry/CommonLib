package yanry.lib.java.model;

import yanry.lib.java.model.watch.ValueHolderImpl;
import yanry.lib.java.util.BitUtil;

/**
 * Created by yanry on 2019/12/15.
 */
public class FlagsHolder extends ValueHolderImpl<Integer> {
    private boolean indexBase;

    public FlagsHolder(boolean indexBase) {
        this.indexBase = indexBase;
    }

    public boolean isIndexBase() {
        return indexBase;
    }

    public boolean hasFlag(int value) {
        return BitUtil.hasFlag(getValue(), value, indexBase);
    }

    public FlagsHolder addFlag(int value) {
        setValue(BitUtil.addFlag(getValue(), value, indexBase));
        return this;
    }

    public FlagsHolder removeFlag(int value) {
        setValue(BitUtil.removeFlag(getValue(), value, indexBase));
        return this;
    }
}
