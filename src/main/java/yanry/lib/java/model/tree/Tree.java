package yanry.lib.java.model.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @param <I>
 *            type of node id.
 * @param <N>
 *            type of node.
 * @author yanry
 *
 *         2016年1月31日
 */
public abstract class Tree<I, N extends Node<I>> {
	private List<N> rootNode;
	private Map<I, N> buildMap;

	public Tree() {
		rootNode = new LinkedList<N>();
		buildMap = new HashMap<I, N>();
	}

	public N getRoot() {
		if (rootNode.size() == 1) {
			return rootNode.get(0);
		} else {
			throw new IllegalStateException(String.format("found %s root nodes instead of 1.", rootNode.size()));
		}
	}

	public N getNode(I id, boolean createIdIfNotExist) {
		N node = buildMap.get(id);
		if (node == null && createIdIfNotExist) {
			node = createNode(id);
			buildMap.put(id, node);
		}
		return node;
	}

	public Tree<I, N> build(I childId, I parentId) {
		N child = getNode(childId, true);
		rootNode.remove(child);
		N parent = getNode(parentId, true);
		child.setParent(parent);
		if (parent.getParent() == null && !rootNode.contains(parent)) {
			rootNode.add(parent);
		}
		return this;
	}

	protected abstract N createNode(I id);
}
