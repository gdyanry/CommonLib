/**
 * 
 */
package lib.common.model.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author yanry
 * 
 *         2014-4-29 下午1:56:13
 */
public abstract class JDBCDao extends BaseDao {

	public abstract Connection getConnection() throws SQLException;

}
