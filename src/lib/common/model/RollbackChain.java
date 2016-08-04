package lib.common.model;

import java.util.Deque;
import java.util.LinkedList;

/**
 * An ordered chain of {@link Rollbackable} objects.
 * 
 * @author yanry
 *
 *         2015年7月17日 下午1:02:54
 */
public class RollbackChain {
	private Deque<Rollbackable> queue;

	public RollbackChain() {
		queue = new LinkedList<Rollbackable>();
	}

	public void reset() {
		queue.clear();
	}

	public void addOperation(Rollbackable op) {
		op.normalOp();
		queue.addFirst(op);
	}

	public void rollback() {
		Rollbackable op;
		while ((op = queue.pollFirst()) != null) {
			op.rollbackOp();
		}
	}

	/**
	 * Represents an operation that supports rollback mechanism.
	 * 
	 * @author yanry
	 *
	 *         2015年7月17日 下午1:07:38
	 */
	public static interface Rollbackable {
		/**
		 * Forward steps of this operation.
		 */
		void normalOp();

		/**
		 * Backward steps of this operation.
		 */
		void rollbackOp();
	}
}
