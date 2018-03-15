/**
 * 
 */
package lib.common.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lib.common.entity.InfoHandler;

/**
 * @author yanry
 *
 *         2015年8月4日 上午10:57:58
 */
public class DbUtil {

	public static void releaseConnection(Connection conn, Statement stmt, ResultSet rs, InfoHandler ih) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			if (ih != null) {
				ih.handleException(e);
			} else {
				e.printStackTrace();
			}
		}
	}
}
