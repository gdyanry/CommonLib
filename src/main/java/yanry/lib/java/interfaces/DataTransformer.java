package yanry.lib.java.interfaces;

/**
 * Created by yanry on 2020/2/4.
 */
public interface DataTransformer<I, O> {
    O transform(I input);
}
