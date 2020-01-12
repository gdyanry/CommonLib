package yanry.lib.java.model.process;

/**
 * Created by yanry on 2020/1/12.
 */
public interface ProcessRequest<D, R> {
    D getRequestData();

    boolean isOpen();

    boolean fail();

    boolean hit(R result);
}
