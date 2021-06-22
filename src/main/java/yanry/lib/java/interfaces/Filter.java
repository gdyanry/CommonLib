package yanry.lib.java.interfaces;

public interface Filter<T> {
    boolean accept(T target);
}
