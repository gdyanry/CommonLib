package lib.common.model.log;

public interface InfoTransformer<T> {
    Object transform(T source);
}
