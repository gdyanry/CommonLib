package yanry.lib.java.model.revert;

import java.util.ArrayList;
import java.util.HashSet;

public class RevertibleSet<E> {
    private RevertManager manager;
    private HashSet<E> set;

    public RevertibleSet(RevertManager manager) {
        this.manager = manager;
        set = new HashSet<>();
    }

    public boolean add(E element) {
        if (!set.contains(element)) {
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    set.add(element);
                }

                @Override
                public void recover() {
                    set.remove(element);
                }
            });
            return true;
        }
        return false;
    }

    public boolean remove(E element) {
        if (set.contains(element)) {
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    set.remove(element);
                }

                @Override
                public void recover() {
                    set.add(element);
                }
            });
            return true;
        }
        return false;
    }

    public void clear() {
        if (!set.isEmpty()) {
            ArrayList<E> copy = new ArrayList<>(set);
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    set.clear();
                }

                @Override
                public void recover() {
                    set.addAll(copy);
                }
            });
        }
    }

    public ArrayList<E> getList() {
        return new ArrayList<>(set);
    }

    public boolean contains(E element) {
        return set.contains(element);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }
}
