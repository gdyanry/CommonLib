package lib.common.model.revert;

import lib.common.model.log.Logger;

import java.util.HashMap;
import java.util.LinkedList;

public class RevertManager {
    private LinkedList<Revertible> stack;
    private HashMap<Object, Tag> tags;

    public RevertManager() {
        stack = new LinkedList<>();
        tags = new HashMap<>();
    }

    public void proceed(Revertible step) {
        if (stack.size() > 0) {
            stack.push(step);
        }
        step.proceed();
    }

    public void revertLastTag() {
        while (stack.size() > 0) {
            Revertible step = stack.pop();
            if (step instanceof Tag) {
                tags.remove(((Tag) step).tag);
                return;
            }
            step.recover();
        }
    }

    public void revertAll() {
        while (stack.size() > 0) {
            stack.pop().recover();
        }
        tags.clear();
    }

    public void tag(Object tag) {
        if (tag == null) {
            return;
        }
        if (tags.containsKey(tag)) {
            throw new IllegalArgumentException("tag already exist: " + tag);
        }
        Tag tagStep = new Tag(tag);
        tags.put(tag, tagStep);
        stack.push(tagStep);
    }

    public void revert(Object tag) {
        if (tag == null) {
            return;
        }
        Tag tagStep = tags.remove(tag);
        if (tagStep == null) {
            throw new IllegalArgumentException("tag doesn't exist: " + tag);
        }
        while (stack.size() > 0) {
            Revertible step = stack.pop();
            if (step instanceof Tag) {
                if (step == tagStep) {
                    return;
                } else {
                    tags.remove(((Tag) step).tag);
                }
            } else {
                step.recover();
            }
        }
        Logger.getDefault().ee("we are not supposed to get here.");
    }

    public int getTagCount() {
        return tags.size();
    }

    public void clean() {
        stack.clear();
        tags.clear();
    }

    private static class Tag implements Revertible {
        private Object tag;

        private Tag(Object tag) {
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
