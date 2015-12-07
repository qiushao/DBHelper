package net.qiushao.lib.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import net.qiushao.lib.dbhelper.annotation.Column;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBHelper<T> extends SQLiteOpenHelper {

    private static boolean debug = true;

    private Class<?> claz;
    private SQLiteDatabase db;
    private SQLiteStatement insertStatement;
    private SQLiteStatement insertOrReplaceStatement;
    private SQLiteStatement insertOrIgnoreStatement;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();

    private String dbName;
    private String tableName;
    private int tableVersion;
    private String createTableSql;
    private String insertSql;
    private String insertOrReplaceSql;
    private String insertOrIgnoreSql;
    private LinkedList<ColumnInfo> columns;
    private LinkedList<ColumnInfo> primaryColumns;
    private ColumnInfo id;

    public DBHelper(Context context, String dir, String dbName, String tableName, int version, Class<?> claz, boolean isPublic) {
        super(new CustomPathDatabaseContext(context, dir, isPublic), dbName, null, version);
        this.dbName = dbName;
        this.tableName = tableName;
        tableVersion = version;
        this.claz = claz;
        initDatabaseInfo();
        db = getWritableDatabase();
        db.execSQL(createTableSql);
        insertStatement = db.compileStatement(insertSql);
        insertOrReplaceStatement = db.compileStatement(insertOrReplaceSql);
        insertOrIgnoreStatement = db.compileStatement(insertOrIgnoreSql);
    }

    /**
     * 插入一个对象到数据库中
     *
     * @param object 要插入数据库的对象
     */
    public void insert(T object) {
        writeLock.lock();
        try {
            bindInsertStatementArgs(insertStatement, object);
            insertStatement.executeInsert();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 如果数据库中已经存在相同的主键了，则更新数据，
     * 否则插入对象到数据库
     *
     * @param object 要插入数据库的对象
     */
    public void insertOrReplace(T object) {
        writeLock.lock();
        try {
            bindInsertStatementArgs(insertOrReplaceStatement, object);
            insertOrReplaceStatement.execute();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 如果数据库中已经存在相同的主键了，则啥都不干，
     * 否则插入对象到数据库
     *
     * @param object 要插入数据库的对象
     */
    public void insertOrIgnore(T object) {
        writeLock.lock();
        try {
            bindInsertStatementArgs(insertOrIgnoreStatement, object);
            insertOrIgnoreStatement.execute();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 插入Collection 集合中的所有元素到数据库
     *
     * @param objects
     */
    public void insertAll(Collection<T> objects) {
        writeLock.lock();
        db.beginTransaction();
        try {
            for (T object : objects) {
                bindInsertStatementArgs(insertStatement, object);
                insertStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            writeLock.unlock();
        }
    }

    /**
     * 删除满足条件的数据
     * 例：db.delete("id=?", new Object[]{"1"})
     *
     * @param whereClause 条件表达式
     * @param whereArgs   条件表达式的参数
     */
    public void delete(String whereClause, Object[] whereArgs) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ");
        sql.append(tableName);
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" where ");
            sql.append(whereClause);
        }

        writeLock.lock();
        try {
            db.execSQL(sql.toString(), whereArgs);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清空数据库
     */
    public void clean() {
        writeLock.lock();
        try {
            db.execSQL("delete from " + tableName);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int update(ContentValues values, String whereClause, String[] whereArgs) {
        readLock.lock();
        try {
            return db.update(tableName, values, whereClause, whereArgs);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 条件查询
     *
     * @param whereClause 条件表达式
     * @param args        条件表达式的参数
     * @return 返回满足条件的对象
     */
    public Collection<T> query(String whereClause, String[] args) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(tableName);
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        readLock.lock();
        try {
            Cursor cursor = db.rawQuery(sql.toString(), args);
            return cursorToObjects(cursor);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 根据数据库主键查询
     *
     * @param keys 主键键值
     * @return 主键为 keys 的元素
     */
    public T queryByPrimary(String[] keys) {
        if (primaryColumns.size() == 0) {
            throw new RuntimeException("you should specify the primary key");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(tableName);
        sql.append(" where ");

        boolean firstCondition = true;
        for (ColumnInfo column : primaryColumns) {
            if (firstCondition) {
                firstCondition = false;
                sql.append(column.name);
                sql.append("=?");
            } else {
                sql.append(" and ");
                sql.append(column.name);
                sql.append("=?");
            }
        }

        readLock.lock();
        try {
            Cursor cursor = db.rawQuery(sql.toString(), keys);
            Collection<T> objects = cursorToObjects(cursor);
            for (T object : objects) {
                return object;
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 直接写数据库语句查询，有时候查询的条件比较复杂，比较嵌套查询，联表查询
     * 使用query(String whereClause, String[] args)方法不能满足，
     * 则可以使用此方法来查询
     *
     * @param sql  完整的数据库语句
     * @param args 数据库语句中"?"的替换参数
     * @return 满足条件的结果集
     */
    public Cursor rawQuery(String sql, String[] args) {
        readLock.lock();
        try {
            return db.rawQuery(sql, args);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 执行不带返回值的数据库语句
     *
     * @param sql 完整的数据库语句
     */
    public void execSQL(String sql) {
        writeLock.lock();
        try {
            db.execSQL(sql);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * 执行不带返回值的数据库语句
     *
     * @param sql  完整的数据库语句
     * @param args 数据库语句中"?"的替换参数
     */
    public void execSQL(String sql, Object[] args) {
        writeLock.lock();
        try {
            db.execSQL(sql, args);
        } finally {
            writeLock.unlock();
        }
    }

    public Collection<T> cursorToObjects(Cursor cursor) {
        LinkedList<T> list = new LinkedList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                T object = newInstance(cursor);
                if (object != null) {
                    list.add(object);
                }
            }
        }
        cursor.close();
        return list;
    }

    public T newInstance(Cursor cursor) {
        Object object = null;
        try {
            object = claz.newInstance();
            if (id != null) {
                id.field.set(object,
                        id.type.getValue(cursor, id.index));
            }
            for (ColumnInfo column : columns) {
                column.field.set(object, column.type.getValue(cursor, column.index));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return (T) object;
    }

    private void bindInsertStatementArgs(SQLiteStatement statement, Object object) {
        int inc = (id == null ? 1 : 0);
        Log.d("qiushao", "inc = " + inc);
        try {
            for (ColumnInfo column : columns) {
                column.type.bindArg(statement, column.index + inc, column.field.get(object));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initDatabaseInfo() {
        columns = new LinkedList<ColumnInfo>();
        primaryColumns = new LinkedList<ColumnInfo>();

        Field[] fields = claz.getDeclaredFields();
        int index = 0;
        for (Field field : fields) {
            if (!DBType.isSupportType(field.getType())) continue;
            Column column = field.getAnnotation(Column.class);
            if (column == null) continue;

            field.setAccessible(true);
            ColumnInfo columnInfo = new ColumnInfo(
                    field,
                    column.name(),
                    DBType.getDBType(field.getType()),
                    column.index()
            );

            if (columnInfo.name.equals("")) {
                columnInfo.name = field.getName();
            }

            if (columnInfo.index == -1) {
                columnInfo.index = index;
                index++;
            }

            if (column.primary()) {
                primaryColumns.add(columnInfo);
            }

            if (column.ID()) {
                Log.d("qiushao", "ID = true");
                if (id != null) {
                    throw new RuntimeException("ID can't be set more than one times!");
                }
                id = columnInfo;
                if (!id.type.getName().equals("INTEGER")) {
                    throw new RuntimeException("ID column must be integer type");
                }
            } else {
                columns.add(columnInfo);
            }
        }

        Collections.sort(columns, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo lhs, ColumnInfo rhs) {
                return lhs.index - rhs.index;
            }
        });

        if (id != null && id.index != 0) {
            columns.get(0).index = id.index;
            id.index = 0;
        }

        genCreateTableSql();
        genInsertSql();
        if (debug) {
            System.out.println("createTableSql = " + createTableSql);
            System.out.println("insertSql = " + insertSql);
        }
    }

    private void genCreateTableSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
        if (id != null) {
            sql.append("(");
            sql.append(id.name);
            sql.append(" integer primary key autoincrement, ");
        } else {
            sql.append("(");
        }

        for (ColumnInfo column : columns) {
            sql.append(column.name);
            sql.append(" ");
            sql.append(column.type.getName());
            sql.append(",");
        }

        if (primaryColumns.size() > 0) {
            sql.append("primary key(");
            for (ColumnInfo column : primaryColumns) {
                sql.append(column.name);
                sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append("))");
        } else {
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        }
        createTableSql = sql.toString();
    }

    private void genInsertSql() {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT OR REPLACE INTO ");
        sql.append(tableName);
        sql.append("(");

        StringBuilder values = new StringBuilder();
        values.append(" VALUES(");

        for (ColumnInfo column : columns) {
            sql.append(column.name);
            sql.append(",");
            values.append("?,");
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        values.deleteCharAt(values.length() - 1);
        values.append(")");

        sql.append(values);
        insertOrReplaceSql = sql.toString();
        insertOrIgnoreSql = insertOrReplaceSql.replaceFirst("OR REPLACE ", "OR IGNORE ");
        insertSql = insertOrReplaceSql.replaceFirst("OR REPLACE ", "");
    }

    public String getDBName() {
        return dbName;
    }

    public int getTableVersion() {
        return tableVersion;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }
}
