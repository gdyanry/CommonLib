package yanry.lib.java.model.process;

import yanry.lib.java.model.uml.UmlElement;

/**
 * 处理结果，包含了处理过程的节点和时间信息。
 * <p>
 * Created by yanry on 2020/5/5.
 */
public class ProcessResult {
    @UmlElement()
    private ProcessNode<?, ? extends ProcessResult> processNode;
    private long endTime;

    void end(ProcessNode<?, ? extends ProcessResult> processNode) {
        this.processNode = processNode;
        endTime = System.currentTimeMillis();
    }

    public ProcessNode<?, ? extends ProcessResult> getProcessNode() {
        return processNode;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getElapsedTime() {
        return endTime - processNode.getRoot().getStartTime();
    }
}
