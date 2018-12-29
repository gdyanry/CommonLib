package lib.common.revert;

import java.util.HashMap;
import java.util.LinkedList;

public class RevertManager {
    private LinkedList<Revertible> stack;
    private HashMap<Object, TagStep> tags;

    public RevertManager() {
        stack = new LinkedList<>();
        tags = new HashMap<>();
    }

    public void proceed(Revertible step) {
        if (!stack.isEmpty()) {
            stack.addFirst(step);
        }
        step.proceed();
    }

    public void tag(Object tag) {
        if (tag == null) {
            return;
        }
        TagStep tagStep = tags.get(tag);
        if (tagStep == null) {
            tagStep = new TagStep(tag);
            tags.put(tag, tagStep);
        }
        stack.addFirst(tagStep);
    }

    public void revertTo(Object tag) {
        Revertible step;
        while ((step = stack.pollFirst()) != null) {
            if (step instanceof TagStep) {
                TagStep tagStep = (TagStep) step;
                if (tagStep.tag.equals(tag)) {
                    return;
                }
            } else {
                step.recover();
            }
        }
    }

    public void revert() {
        Revertible step;
        while ((step = stack.pollFirst()) != null) {
            if (!(step instanceof TagStep)) {
                step.recover();
                return;
            }
        }
    }

    public void revertAll() {
        Revertible step;
        while ((step = stack.pollFirst()) != null) {
            step.recover();
        }
    }

    private static class TagStep implements Revertible {
        private Object tag;

        private TagStep(Object tag) {
            this.tag = tag;
        }

        @Override
        public void proceed() {

        }

        @Override
        public void recover() {

        }
    }
}
