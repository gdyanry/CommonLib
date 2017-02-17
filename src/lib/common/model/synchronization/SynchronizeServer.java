package lib.common.model.synchronization;

import lib.common.entity.InfoHandler;
import lib.common.model.dao.JDBCDML;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.util.ReflectionUtil;

import java.sql.*;

/**
 * Created by rongyu.yan on 12/14/2016.
 */

public abstract class SynchronizeServer {
    public SynchronizeServer() {
        ReflectionUtil.initStaticStringFields(SyncConst.class);
    }

    public JSONObject processRequest(String table, final JSONObject data, final int userId) {
//        请求数据格式：
//        {"sync_time":sync_time,"created":[{"id":CLIENT_ID, ...}, ...],"modified":[{"server_id":SERVER_ID, ...}, ...],"deleted":[SERVER_ID, ...]}
        final long curTime = System.currentTimeMillis();
        final JSONObject resp = new JSONObject().put(ClientControlTable.sync_time, curTime);
        // created
        JSONArray created = data.optJSONArray(SyncConst.created);
        if (created != null) {
            String insertSql = null;
            for (int i = 0; i < created.length(); i++) {
                final JSONObject item = created.getJSONObject(i);
                final Object clientId = item.get(SyncConst.client_id);
                item.remove(SyncConst.client_id);
                if (insertSql == null) {
                    // create insert sql statement
                    StringBuilder sb = new StringBuilder("insert into ").append(table).append("(").append(ServerObjectTable.if_delete)
                            .append(",").append(ServerObjectTable.update_timestamp).append(",").append(ServerObjectTable.user_id);
                    for (String key : item.keySet()) {
                        sb.append(",").append(key);
                    }
                    sb.append(")values(?,?,?");
                    for (int j = 0; j < item.length(); j++) {
                        sb.append(",?");
                    }
                    sb.append(")");
                    insertSql = sb.toString();
                }
                // insert
                new JDBCDML(false, false, getConnection(), insertSql, getInfoHandler()) {

                    @Override
                    protected void onResult(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            resp.append(SyncConst.created, new JSONArray().put(clientId).put(rs.getObject(1)));
                        }
                    }

                    @Override
                    protected void setParameters(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, 0);
                        ps.setLong(2, curTime);
                        ps.setInt(3, userId);
                        int i = 4;
                        for (String key : item.keySet()) {
                            ps.setObject(i++, item.get(key));
                        }
                    }
                };
            }
        }
        // modified
        JSONArray modified = data.optJSONArray(SyncConst.modified);
        if (modified != null) {
            String updateSql = null;
            for (int i = 0; i < modified.length(); i++) {
                final JSONObject item = modified.getJSONObject(i);
                final Object serverId = item.get(SyncConst.server_id);
                item.remove(SyncConst.server_id);
                if (updateSql == null) {
                    // create update sql statement
                    StringBuilder sb = new StringBuilder("update ").append(table).append(" set ").append(ServerObjectTable.update_timestamp)
                            .append("=").append("?");
                    for (String key : item.keySet()) {
                        sb.append(",").append(key).append("=?");
                    }
                    sb.append(" where ").append(SyncConst.server_id).append("=?");
                    updateSql = sb.toString();
                }
                // update
                new JDBCDML(false, true, getConnection(), updateSql, getInfoHandler()) {

                    @Override
                    protected void onResult(ResultSet rs) throws SQLException {
                        resp.append(SyncConst.modified, serverId);
                    }

                    @Override
                    protected void setParameters(PreparedStatement ps) throws SQLException {
                        ps.setLong(1, curTime);
                        int i = 2;
                        for (String key : item.keySet()) {
                            ps.setObject(i++, item.get(key));
                        }
                        ps.setObject(i, serverId);
                    }
                };
            }
        }
        // deleted
        final JSONArray deleted = data.optJSONArray(SyncConst.deleted);
        if (deleted != null) {
            StringBuilder sb = new StringBuilder("update ").append(table).append(" set ").append(ServerObjectTable.update_timestamp)
                    .append("=").append(curTime).append(",").append(ServerObjectTable.if_delete).append("=1").append(" where ")
                    .append(SyncConst.server_id).append(" in(");
            for (int i = 0; i < deleted.length(); i++) {
                sb.append(deleted.get(i)).append(",");
            }
            sb.deleteCharAt(sb.length() - 1).append(")");
            new JDBCDML(false, true, getConnection(), sb.toString(), getInfoHandler()) {
                @Override
                protected void onResult(ResultSet rs) throws SQLException {
                    resp.append(SyncConst.deleted, deleted);
                }
            };
        }
        // push
        String querySql = String.format("select * from %s where %s=? and %s>? and %<s<?", table, ServerObjectTable.user_id,
                ServerObjectTable.update_timestamp);
        new JDBCDML(true, true, getConnection(), querySql, getInfoHandler()) {
            @Override
            protected void setParameters(PreparedStatement ps) throws SQLException {
                ps.setObject(1, userId);
                ps.setLong(2, data.getLong(ClientControlTable.sync_time));
                ps.setLong(3, curTime);
            }

            @Override
            protected void onResult(ResultSet rs) throws SQLException {
                JSONObject pushResp = new JSONObject();
                resp.put(SyncConst.push, pushResp);
                while (rs.next()) {
                    if (rs.getInt(ServerObjectTable.if_delete) == 1) {
                        pushResp.append(SyncConst.deleted, rs.getObject(SyncConst.server_id));
                    } else {
                        JSONObject item = new JSONObject();
                        pushResp.append(SyncConst.modified, item);
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            if (columnName.equals(SyncConst.server_id)) {
                                item.put(SyncConst.server_id, rs.getObject(i));
                            } else if (!columnName.equals(ServerObjectTable.update_timestamp) && !columnName.equals(ServerObjectTable.if_delete)
                                    && !columnName.equals(ServerObjectTable.user_id)) {
                                item.put(columnName, rs.getObject(i));
                            }
                        }
                    }
                }
            }
        };
        return resp;
    }

    protected abstract Connection getConnection();

    protected abstract InfoHandler getInfoHandler();
}
