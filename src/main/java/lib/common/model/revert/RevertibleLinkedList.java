package lib.common.model.revert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class RevertibleLinkedList<E> {
    private RevertManager manager;
    private LinkedList<E> list;

    public RevertibleLinkedList(RevertManager manager) {
        this.manager = manager;
        list = new LinkedList<>();
    }

    public void push(E e) {
        manager.proceed(new Revertible() {
            @Override
            public void proceed() {
                list.addFirst(e);
            }

            @Override
            public void recover() {
                list.removeFirst();
            }
        });
    }

    public E pop() {
        if (list.size() > 0) {
            E removed = list.removeFirst();
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                }

                @Override
                public void recover() {
                    list.addFirst(removed);
                }
            });
            return removed;
        }
        return null;
    }

    public void addLast(E e) {
        manager.proceed(new Revertible() {
            @Override
            public void proceed() {
                list.addLast(e);
            }

            @Override
            public void recover() {
                list.removeLast();
            }
        });
    }

    public E removeLast() {
        if (list.size() > 0) {
            E removed = list.removeLast();
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                }

                @Override
                public void recover() {
                    list.addLast(removed);
                }
            });
            return removed;
        }
        return null;
    }

    public void addAll(Collection<E> collection) {
        int count = collection.size();
        if (count > 0) {
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    list.addAll(collection);
                }

                @Override
                public void recover() {
                    for (int i = 0; i < count; i++) {
                        list.removeLast();
                    }
                }
            });
        }
    }

    public boolean remove(E element) {
        LinkedList<E> copy = new LinkedList<>(list);
        if (list.remove(element)) {
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                }

                @Override
                public void recover() {
                    list = copy;
                }
            });
            return true;
        }
        return false;
    }

    public void removeAll(Collection<E> collection) {
        if (collection.size() > 0) {
            LinkedList<E> copy = new LinkedList<>(list);
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    list.removeAll(collection);
                }

                @Override
                public void recover() {
                    list = copy;
                }
            });
        }
    }

    public void clear() {
        if (list.size() > 0) {
            ArrayList<E> copy = new ArrayList<>(list);
            manager.proceed(new Revertible() {
                @Override
                public void proceed() {
                    list.clear();
                }

                @Override
                public void recover() {
                    list.addAll(copy);
                }
            });
        }
    }

    public Iterator<E> iterator() {
        LinkedList<E> copy = new LinkedList<>(list);
        manager.proceed(new Revertible() {
            @Override
            public void proceed() {
            }

            @Override
            public void recover() {
                list = copy;
            }
        });
        return list.iterator();
    }

    public ArrayList<E> getList() {
        return new ArrayList<>(list);
    }

    public E get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.size() == 0;
    }

    public boolean contains(E element) {
        return list.contains(element);
    }

    public E peekFirst() {
        return list.peekFirst();
    }

    public E peekLast() {
        return list.peekLast();
    }
}
