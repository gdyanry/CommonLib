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
public abstract class JDBCDML<R> {
	private R result;
	private Connection conn;

	/**
	 * 
	 * @param isQuery
	 * @param isSimple simple means no extra parameter when calling {@link Connection#prepareStatement(String)}.
	 */
	public JDBCDML(boolean isQuery, boolean isSimple) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getDao().getConnection();
			ps = prepareStatement(getSql(), isQuery, isSimple);
			setParameters(ps);
			ps.execute();
			if (isQuery) {
				rs = ps.executeQuery();
			} else if (!isSimple) {
				rs = ps.getGeneratedKeys();
			}
			result = onExecuted(rs);
		} catch (SQLException e) {
			getInfoHandler().handleException(e);
		} finally {
			DbUtil.releaseConnection(conn, ps, rs, getInfoHandler());
		}
	}

	protected PreparedStatement prepareStatement(String sql, boolean isQuery,
			boolean isSimple) throws SQLException {
		return isSimple ? conn.prepareStatement(sql) : isQuery ? conn
				.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE)
				: conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
	}

	public R getResult() {
		return result;
	}

	protected abstract R onExecuted(ResultSet rs) throws SQLException;

	protected abstract JDBCDao getDao() throws SQLException;

	protected abstract String getSql();

	protected abstract void setParameters(PreparedStatement ps)
			throws SQLException;

	protected abstract InfoHandler getInfoHandler();
}
