package yanry.lib.java.model.process;

/**
 * 处理过程的节点信息，用于追溯处理过程信息。
 * <p>
 * Created by yanry on 2020/5/5.
 *
 * @param <D> 待处理的数据类型。
 * @param <R> 处理结果类型。
 */
public interface ProcessNode<D, R extends ProcessResult> {
    /**
     * 获取父节点。
     *
     * @return
     */
    ProcessNode<?, R> getParent();

    /**
     * 获取根节点。
     *
     * @return
     */
    ProcessNode<?, R> getRoot();

    /**
     * 获取当前节点的处理器。
     *
     * @return
     */
    Processor<? super D, R> getProcessor();

    /**
     * 获取当前节点的开始处理时间戳。
     *
     * @return
     */
    long getStartTime();
}
