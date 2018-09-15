/**
 *
 */
package lib.common.util;

import lib.common.model.log.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yanry
 * <p>
 * 2015年8月4日 上午10:57:58
 */
public class DbUtil {

    /**
     * requires jdk 8.
     *
     * @param conn
     * @param stmt
     * @param rs
     */
    public static void releaseConnection(Connection conn, Statement stmt, ResultSet rs) {
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
            Logger.getDefault().catches(e);
        }
    }
}
