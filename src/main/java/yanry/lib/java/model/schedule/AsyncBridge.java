package yanry.lib.java.model.schedule;

public interface AsyncBridge<D extends ShowData, V> {
    V show(D data);
}
