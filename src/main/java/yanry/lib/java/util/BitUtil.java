package yanry.lib.java.util;

public final class BitUtil {
    public static boolean hasFlag(int flags, int value, boolean indexBase) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                return false;
            }
        }
        return (flags & value) == value;
    }

    public static int addFlag(int flags, int value, boolean indexBase) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                throw new IllegalArgumentException("invalid index value: " + value);
            }
        }
        flags |= value;
        return flags;
    }

    public static int removeFlag(int flags, int value, boolean indexBase) {
        if (indexBase) {
            if (value >= 0 && value < 32) {
                value = 1 << value;
            } else {
                throw new IllegalArgumentException("invalid index value: " + value);
            }
        }
        flags &= ~value;
        return flags;
    }

    public static int getSectionValue(int value, int fromShift, int toShift) {
        // todo
        return 0;
    }

    public static boolean setSectionValue(int value, int fromShift, int toShift) {
        // todo
        return false;
    }
}
