/**
 * 
 */
package lib.common.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lib.common.entity.InfoHandler;
import lib.common.util.DbUtil;

/**
 * @author yanry
 *
 *         2015年10月8日
 */
public class JDBCDML {

	/**
	 * 
	 * @param isQuery
	 * @param isSimple simple means no extra parameter when calling {@link Connection#prepareStatement(String)},
	 *                    precisely, not simple means updatable query, or insertion with generated keys.
	 */
	public JDBCDML(boolean isQuery, boolean isSimple, Connection conn, String sql, InfoHandler infoHandler) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = isSimple ? conn.prepareStatement(sql) : isQuery ? conn
					.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_UPDATABLE)
					: conn.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
			setParameters(ps);
			ps.execute();
			if (isQuery) {
				rs = ps.executeQuery();
			} else if (!isSimple) {
				rs = ps.getGeneratedKeys();
			}
			onResult(rs);
		} catch (SQLException e) {
			if (infoHandler != null) {
				infoHandler.handleException(e);
			}
		} finally {
			DbUtil.releaseConnection(conn, ps, rs, infoHandler);
		}
	}

	protected void onResult(ResultSet rs) throws SQLException {}

	protected void setParameters(PreparedStatement ps) throws SQLException {}
}
