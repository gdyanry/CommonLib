/**
 * 
 */
package lib.common.entity;

/**
 * @author Yanry
 * 
 */
public interface InfoHandler {
	int LEVEL_DEBUG = 1;
	int LEVEL_ERROR = 2;
	int LEVEL_EXCEPTION = 3;
	int LEVEL_NONE = 4;
	
	void setLevel(int level);
	
	void handleException(Exception e);

	void handleThrowable(Throwable e);

	/**
	 * Post an error message to user interface.
	 * @param msg
	 */
	void showError(String msg);

	/**
	 * Post an information to user interface.
	 * @param msg
	 */
	void showMessage(String msg);

	/**
	 * Log a debug message.
	 * @param tag current class object, to help locating source code.
	 * @param msg
	 */
	void debug(Class<?> tag, String msg);

	/**
	 * Log an error message.
	 * @param tag current class object, to help locating source code.
	 * @param msg
	 */
	void error(Class<?> tag, String msg);
}
