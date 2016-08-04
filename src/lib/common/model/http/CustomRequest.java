/**
 * 
 */
package lib.common.model.http;

import java.net.HttpURLConnection;

/**
 * @author yanry
 *
 *         2015年12月2日
 */
public interface CustomRequest {
	/**
	 * @param conn
	 *            do anything you like BEFORE
	 *            {@link HttpURLConnection #connect()}.
	 */
	void customize(HttpURLConnection conn);
}
