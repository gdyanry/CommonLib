package yanry.lib.java.model;

/**
 * Created by yanry on 2019/12/15.
 */
public class FlagsHolder {
    private boolean indexBase;
    private int flags;

    public FlagsHolder(boolean indexBase) {
        this.indexBase = indexBase;
    }

    public boolean isIndexBase() {
        return indexBase;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean hasFlag(int value) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                return false;
            }
        }
        return (flags & value) == value;
    }

    public FlagsHolder addFlag(int value) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                throw new IllegalArgumentException("invalid index value: " + value);
            }
        }
        flags |= value;
        return this;
    }

    public FlagsHolder removeFlag(int value) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                throw new IllegalArgumentException("invalid index value: " + value);
            }
        }
        flags &= ~value;
        return this;
    }
}
