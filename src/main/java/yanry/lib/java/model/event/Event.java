package yanry.lib.java.model.event;

import java.util.HashMap;
import java.util.ListIterator;

/**
 * @author: rongyu.yan
 * @create: 2020-07-25 16:01
 **/
public class Event {
    HashMap<EventInterceptor, ListIterator> iteratorCache;

    public Event() {
        iteratorCache = new HashMap<>();
    }

    public int getCurrentLevel() {
        return iteratorCache.size() + 1;
    }
}
