package yanry.lib.java.model.synchronization;

import yanry.lib.java.model.dao.BaseDao;

/**
 * Created by rongyu.yan on 12/13/2016.
 */

public abstract class ClientControlTable extends BaseDao.Table {
    /**
     * 同步的对象是以数据库表为单位的
     */
    public static String table;
    /**
     * 每次同步完成后记录的服务器时间
     */
    public static String sync_time;
    /**
     * 发起同步请求时，该表中相关记录的状态被改为synchronizing，即不可重复操作，所以需要记录该次请求的数据，以便请求失败时重新发起请求。
     * 可在请求成功后清空该字段
     */
    public static String request_data;
    /**
     * 用于标识当前的同步请求是不是重发之前失败的请求，如果是则在同步成功后还需要再执行一次同步流程
     */
    public static String if_pending;

    public ClientControlTable(BaseDao dao) {
        dao.super();
    }
}
