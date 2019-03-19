package yanry.lib.java.entity;

/**
 * @author yanry
 *
 *         2016年5月30日
 */
public class CheckedNullPointerException extends Exception {
	private static final long serialVersionUID = -2475310384465334842L;

	public static void check(Object var) throws CheckedNullPointerException {
		if (var == null) {
			throw new CheckedNullPointerException();
		}
	}
}
