package yanry.lib.java.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yanry on 2020/5/8.
 */
public class Registry<T> {
    private List<T> registrantList = Collections.EMPTY_LIST;
    private Comparator<T> comparator;

    /**
     * 设置排序比较器，设置后再调用{@link #register(Object[])}时生效。
     *
     * @param comparator
     */
    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public boolean register(T... registrants) {
        if (registrants == null || registrants.length == 0) {
            return false;
        }
        synchronized (this) {
            ArrayList<T> newList = new ArrayList<>(registrantList.size() + registrants.length);
            if (registrantList.size() > 0) {
                newList.addAll(registrantList);
            }
            boolean changed = false;
            for (T registrant : registrants) {
                if (registrant != null && !newList.contains(registrant)) {
                    newList.add(registrant);
                    changed = true;
                }
            }
            if (changed) {
                if (comparator != null) {
                    Collections.sort(newList, comparator);
                }
                registrantList = Collections.unmodifiableList(newList);
                return true;
            }
        }
        return false;
    }

    public boolean unregister(T... registrants) {
        if (registrants == null || registrants.length == 0) {
            return false;
        }
        synchronized (this) {
            if (registrantList.size() == 0) {
                return false;
            }
            ArrayList<T> newList = new ArrayList<>(registrantList);
            boolean changed = false;
            for (T registrant : registrants) {
                if (registrant != null && newList.remove(registrant)) {
                    changed = true;
                }
            }
            if (changed) {
                registrantList = Collections.unmodifiableList(newList);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取内部只读List，不为null且不可修改
     *
     * @return
     */
    public List<T> getList() {
        return registrantList;
    }
}
