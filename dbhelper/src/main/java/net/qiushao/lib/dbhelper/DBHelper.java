package net.qiushao.lib.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import net.qiushao.lib.dbhelper.annotation.ID;
import net.qiushao.lib.dbhelper.annotation.Primary;
import net.qiushao.lib.dbhelper.annotation.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBHelper<T> extends SQLiteOpenHelper {

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

    <T> DBHelper(Context context, Class<T> claz, String dbName, int version) {
        super(context, dbName, null, version);
        this.dbName = dbName;
        this.tableName = claz.getSimpleName();
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
            bindInsertOrReplaceStatementArgs(insertOrReplaceStatement, object);
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
            bindInsertOrReplaceStatementArgs(insertOrIgnoreStatement, object);
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
            db.execSQL("DELETE FROM sqlite_sequence"); //自增列归零
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
    public List<T> query(String whereClause, String[] args) {
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
     * 直接写数据库语句查询，有时候查询的条件比较复杂，比如嵌套查询，
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

    /**
     * @return 数据库表数据量，即有多少个记录
     */
    public long size() {
        Cursor cursor = db.rawQuery("select count(*) from " + getTableName(), null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    public List<T> cursorToObjects(Cursor cursor) {
        ArrayList<T> list = new ArrayList<>();
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
        try {
            int index = 1;
            for (ColumnInfo column : columns) {
                if (column.isID) continue;
                column.type.bindArg(statement, index++, column.field.get(object));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void bindInsertOrReplaceStatementArgs(SQLiteStatement statement, Object object) {
        try {
            int index = 1;
            for (ColumnInfo column : columns) {
                column.type.bindArg(statement, index++, column.field.get(object));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initDatabaseInfo() {
        collectColumns();
        genCreateTableSql();
        genInsertSql();
    }

    private void collectColumns() {
        columns = new LinkedList<ColumnInfo>();
        primaryColumns = new LinkedList<ColumnInfo>();
        Field[] fields = claz.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers()) || !DBType.isSupportType(field.getType()))
                continue;
            field.setAccessible(true);
            ColumnInfo columnInfo = new ColumnInfo(field, field.getName(), DBType.getDBType(field.getType()));
            if (null != field.getAnnotation(Primary.class)) {
                primaryColumns.add(columnInfo);
            }
            if (null != field.getAnnotation(ID.class)) {
                columnInfo.isID = true;
            }
            if (null != field.getAnnotation(Unique.class)) {
                columnInfo.isUnique = true;
            }
            columns.add(columnInfo);
            Debug.d("add column : " + columnInfo.name);
        }
    }

    private void genCreateTableSql() {
        int index = 0;
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
        sql.append("(");

        for (ColumnInfo column : columns) {
            column.index = index++;
            if (column.isID) {
                sql.append(column.name);
                sql.append(" integer primary key autoincrement,");
            } else {
                sql.append(column.name);
                sql.append(" ");
                sql.append(column.type.getName());
                if (column.isUnique) {
                    sql.append(" UNIQUE");
                }
                sql.append(",");
            }
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
        StringBuilder insertSB = new StringBuilder();
        StringBuilder insertOrReplaceSB = new StringBuilder();

        insertSB.append("INSERT INTO ");
        insertSB.append(tableName);
        insertSB.append("(");

        insertOrReplaceSB.append("INSERT OR REPLACE INTO ");
        insertOrReplaceSB.append(tableName);
        insertOrReplaceSB.append("(");

        StringBuilder insertValues = new StringBuilder();
        StringBuilder insertOrReplaceValues = new StringBuilder();
        insertValues.append(" VALUES(");
        insertOrReplaceValues.append(" VALUES(");

        for (ColumnInfo column : columns) {
            if (column.isID) {
                insertOrReplaceSB.append(column.name);
                insertOrReplaceSB.append(",");
                insertOrReplaceValues.append("?,");
                continue;
            };
            insertSB.append(column.name);
            insertSB.append(",");
            insertOrReplaceSB.append(column.name);
            insertOrReplaceSB.append(",");
            insertValues.append("?,");
            insertOrReplaceValues.append("?,");
        }

        insertSB.deleteCharAt(insertSB.length() - 1);
        insertSB.append(")");
        insertOrReplaceSB.deleteCharAt(insertOrReplaceSB.length() - 1);
        insertOrReplaceSB.append(")");

        insertValues.deleteCharAt(insertValues.length() - 1);
        insertValues.append(")");
        insertOrReplaceValues.deleteCharAt(insertOrReplaceValues.length() - 1);
        insertOrReplaceValues.append(")");

        insertSB.append(insertValues);
        insertOrReplaceSB.append(insertOrReplaceValues);

        insertSql = insertSB.toString();
        insertOrReplaceSql = insertOrReplaceSB.toString();
        insertOrIgnoreSql = insertOrReplaceSql.replaceFirst("OR REPLACE ", "OR IGNORE ");

        Debug.d("insert sql = " + insertSql);
        Debug.d("insert or replace sql = " + insertOrReplaceSql);
        Debug.d("insert or ignore sql = " + insertOrIgnoreSql);
    }

    public String getDBName() {
        return dbName;
    }

    public int getDBVersion() {
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
