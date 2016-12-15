package lib.common.model.synchronization;

import lib.common.model.dao.BaseDao;

/**
 * 服务端需要同步的表基本结构
 * Created by rongyu.yan on 12/13/2016.
 */

public abstract class ServerObjectTable extends BaseDao.Table {
    public static String update_timestamp;
    public static String if_delete;
    public static String user_id;

    public ServerObjectTable(BaseDao dao) {
        dao.super();
    }
}
