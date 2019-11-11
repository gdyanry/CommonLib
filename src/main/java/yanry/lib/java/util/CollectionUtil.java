package yanry.lib.java.util;

import yanry.lib.java.model.Singletons;

import java.util.List;
import java.util.Random;

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

    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(Singletons.get(Random.class).nextInt(list.size()));
    }

    public static <T> T getRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[Singletons.get(Random.class).nextInt(array.length)];
    }
}
