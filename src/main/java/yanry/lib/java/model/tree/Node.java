package yanry.lib.java.model.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <I>
 *            type of node id
 * @author yanry
 *
 *         2016年1月31日
 */
public class Node<I> {
	private I id;
	private Map<Object, Node<I>> children;
	private Node<I> parent;

	public Node(I id) {
		this.id = id;
		children = new HashMap<Object, Node<I>>();
	}

	void setParent(Node<I> parent) {
		this.parent = parent;
		parent.children.put(id, this);
	}

	public I getId() {
		return id;
	}

	public Node<I> getParent() {
		return parent;
	}

	public Node<I> getChild(I childId) {
		return children.get(childId);
	}

	public void removeChild(I childId) {
		children.remove(childId);
	}

	public Collection<Node<I>> getChildren() {
		return children.values();
	}

	public String getFullName(TreeNodeDisplayHook<I> hook) {
		return appendName(hook.getNodeText(this), hook, parent);
	}
	
	public boolean isDescendentOf(I Id) {
		return checkEquals(Id, getParent());
	}
	
	private boolean checkEquals(I id, Node<I> node) {
		if (node == null) {
			return false;
		}
		if (node.getId().equals(id)) {
			return true;
		}
		return checkEquals(id, node.getParent());
	}

	private String appendName(String currentName, TreeNodeDisplayHook<I> hook, Node<I> node) {
		if (node == null || hook.getNodeText(node) == null) {
			return currentName;
		} else {
			currentName = String.format("%s%s%s", hook.getNodeText(node), hook.getJoint(), currentName);
			return appendName(currentName, hook, node.parent);
		}
	}
}
