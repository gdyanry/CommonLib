/**
 *
 */
package lib.common.model.dao;

import lib.common.model.log.Logger;
import lib.common.util.DbUtil;

import java.sql.*;

/**
 * requires jkd8.
 *
 * @author yanry
 * <p>
 * 2015年10月8日
 */
public class JDBCDML {

    /**
     * @param isQuery
     * @param isSimple simple means no extra parameter when calling {@link Connection#prepareStatement(String)},
     *                 precisely, not simple means updatable query, or insertion with generated keys.
     */
    public JDBCDML(boolean isQuery, boolean isSimple, Connection conn, String sql) {
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
            Logger.getDefault().catches(e);
        } finally {
            DbUtil.releaseConnection(conn, ps, rs);
        }
    }

    protected void onResult(ResultSet rs) throws SQLException {
    }

    protected void setParameters(PreparedStatement ps) throws SQLException {
    }
}
