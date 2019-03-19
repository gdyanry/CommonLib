package yanry.lib.java.entity;

/**
 * @author yanry
 *
 * 2016年7月8日
 */
public class ActionAbortException extends Exception {
	
	private static final long serialVersionUID = -7179260772590026115L;

	public ActionAbortException() {
	}
	
	public ActionAbortException(String msg) {
		super(msg);
	}
}
