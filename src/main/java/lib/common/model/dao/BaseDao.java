package lib.common.model.dao;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lib.common.util.ReflectionUtil;

/**
 * 该类主要提供了对数据库表和视图定义的一种不同于ORM框架的表示方式，以及对这些表和视图进行基本的管理。
 * @author yanry
 * 
 */
public abstract class BaseDao {

	private List<Table> tables;
	private List<View> views;
	private List<String> initSQL;

	protected BaseDao() {
		tables = new LinkedList<BaseDao.Table>();
		views = new LinkedList<BaseDao.View>();
		initSQL = new LinkedList<String>();
		instantiateDbObjects();
	}

	public List<Table> getTables() {
		return tables;
	}

	public List<View> getViews() {
		return views;
	}

	public List<String> getExtraStatements() {
		return initSQL;
	}

	/**
	 * 
	 * @param executor
	 *            pass null if you don't want the statements to be executed.
	 * @return sql statement of creating database objects that initiated in
	 *         {@link #instantiateDbObjects()}.
	 */
	public String getDbObjectCreateStatements(SqlExecutor executor) {
		StringBuilder sb = new StringBuilder();
		for (Table t : tables) {
			String createTable = t.generateSql();
			sb.append(createTable).append(';').append(System.getProperty("line.separator"));
			if (executor != null) {
				executor.execute(createTable);
			}
			t.statementsAfterCreation(initSQL);
		}
		for (String sql : initSQL) {
			sb.append(sql).append(';').append(System.getProperty("line.separator"));
			if (executor != null) {
				executor.execute(sql);
			}
		}
		for (View v : views) {
			String createView = v.generateSql();
			sb.append(createView).append(';').append(System.getProperty("line.separator"));
			if (executor != null) {
				executor.execute(createView);
			}
		}
		return sb.toString();
	}

	/**
	 * 实例化{@link DBObject}。
	 */
	protected abstract void instantiateDbObjects();

	/**
	 * create table语句的第一行，如: create table if not exists xxx
	 * 
	 * @param tableName
	 * @return
	 */
	protected abstract String getCreateTableStmt(String tableName);

	/**
	 * 例：create view if not exists xxx （不包含as）
	 * 
	 * @param viewName
	 * @return
	 */
	protected abstract String getCreateViewStmt(String viewName);

	/**
	 * Invoke this method in case of using keyword as field name.
	 * 
	 * @param fieldName
	 * @return
	 */
	protected String wrapField(String fieldName) {
		return fieldName;
	}

	/*************************************************************************************************************/

	/**
	 * An abstract interface that is capable to execute sql statement.
	 * 
	 * @author yanry
	 *
	 *         2015年7月27日 下午1:23:09
	 */
	public static interface SqlExecutor {
		void execute(String sql);
	}

	/**
	 * Database object such as table and view. "public static String" fields are
	 * automatically initialized as the field name, and will be used as column
	 * names of this database object.
	 * 
	 * @author yanry
	 *
	 *         2015年7月27日 下午1:24:26
	 */
	public abstract class DBObject {
		public DBObject() {
			// set value of public static fields that represent table column
			// names.
			ReflectionUtil.initStaticStringFields(getClass());
		}

		abstract String generateSql();
	}

	/*****************************************************************************************************************/

	public abstract class Table extends DBObject {

		public Table() {
			tables.add(this);
		}

		/**
		 * 生成当前表的create table语句。
		 */
		@Override
		String generateSql() {
			LinkedHashMap<String, String> name_defSql = new LinkedHashMap<String, String>();
			addColumns(name_defSql);
			StringBuilder sb = new StringBuilder();
			sb.append(getCreateTableStmt(getClass().getSimpleName())).append('(');
			for (Entry<String, String> column : name_defSql.entrySet()) {
				sb.append(wrapField(column.getKey())).append(' ').append(column.getValue()).append(',');
			}
			String constrain = getConstrainStmt();
			if (constrain != null) {
				sb.append(constrain);
			} else {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(')');
			String options = getTableOptions();
			if (options != null) {
				sb.append(options);
			}
			return sb.toString();
		}

		/**
		 * 生成表后如需要立即插入（测试）数据，可在getAfterCreateSql()中调用该方法.
		 * 
		 * @param columns
		 *            列名
		 * @param rows
		 *            每一行对应列的值
		 * @return 插入语句
		 */
		protected String insertSqls(String[] columns, String[][] rows) {
			StringBuilder sb = new StringBuilder("insert into ").append(getClass().getSimpleName()).append('(');
			for (int i = 0; i < columns.length; i++) {
				sb.append(columns[i]).append(',');
			}
			sb.setCharAt(sb.length() - 1, ')');
			sb.append("values");
			for (int i = 0; i < rows.length; i++) {
				sb.append('(');
				for (int j = 0; j <= rows[i].length; j++) {
					sb.append('\'').append(rows[i][j]).append('\'').append(',');
				}
				sb.setCharAt(sb.length() - 1, ')');
				sb.append(',');
			}
			sb.setCharAt(sb.length() - 1, ')');
			return sb.toString();
		}

		/**
		 * Add name and definition of columns to the given map.
		 * 
		 * @param columnDefinition
		 *            列定义语句
		 */
		protected abstract void addColumns(Map<String, String> columnDefinition);

		/**
		 * Definition statement of constrain objects such as primary key,
		 * foreign key, etc, after column definition statement.
		 * 
		 * @return create table语句中的表约束语句。
		 */
		protected abstract String getConstrainStmt();

		/**
		 * 
		 * @return )后面的语句，如：engine=engine_name, comment='string'……
		 */
		protected abstract String getTableOptions();

		/**
		 * create table语句结束后执行的语句。
		 * 
		 * @param stmts
		 *            将要添加的语句放入此集合中
		 */
		protected abstract void statementsAfterCreation(List<String> stmts);

	}

	/*************************************************************************************************/

	public abstract class View extends DBObject {
		public View() {
			views.add(this);
		}

		@Override
		String generateSql() {
			return new StringBuilder(getCreateViewStmt(getClass().getSimpleName())).append(" as ")
					.append(getCreateBody()).toString();
		}

		/**
		 * 
		 * @return create view ... as后面的语句
		 */
		protected abstract String getCreateBody();
	}
}
