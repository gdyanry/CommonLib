package lib.common.util;

import java.util.List;

public class CollectionUtil {
    public static <E> boolean checkLoop(List<E> list, E elementToAdd) {
        int lastOccurrence = list.lastIndexOf(elementToAdd);
        if (lastOccurrence >= 0) {
            int lastIndex = list.size() - 1;
            if (lastOccurrence == lastIndex) {
                return true;
            }
            int distance = list.size() - lastOccurrence;
            if (list.size() > (distance - 1) * 2) {
                for (int i = lastIndex; i > lastOccurrence && i - distance >= 0; i--) {
                    if (!list.get(i).equals(list.get(i - distance))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
