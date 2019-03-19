package yanry.lib.java.entity;

/**
 * @author yanry
 *
 *         2016年7月17日
 */
public class CheckedOutOfMemoryException extends Exception {

	private static final long serialVersionUID = 8090422800375190816L;

	public CheckedOutOfMemoryException() {

	}

	public CheckedOutOfMemoryException(String msg) {
		super(msg);
	}
}
