package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import net.qiushao.lib.dbhelper.annotation.Column;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBHelper<T> extends SQLiteOpenHelper{

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

	public DBHelper(Context context, String dir, String dbName, String tableName, int version, Class<?> claz) {
		super(new CustomPathDatabaseContext(context, dir), dbName, null, version);
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
     * this method requires your table have a primary key,
     * if primary key not in database, that will insert object into database,
     * else update database with new value
     * @param object
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
     * this method requires your table have a primary key,
     * if primary key not in database, that will insert object into database,
     * else this operate will be ignore
     * @param object
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

    public void clean() {
        writeLock.lock();
        try {
            db.execSQL("delete from " + tableName);
        } finally {
            writeLock.unlock();
        }
    }

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
     *
     * @param args
     */
    public T queryByPrimary(String[] args) {
        if(primaryColumns.size() == 0) {
            throw new RuntimeException("you should specify the primary key");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(tableName);
        sql.append(" where ");

        boolean firstCondition = true;
        for(ColumnInfo column : primaryColumns) {
            if(firstCondition) {
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
            Cursor cursor = db.rawQuery(sql.toString(), args);
            Collection<T> objects = cursorToObjects(cursor);
            for(T object : objects) {
                return object;
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public Cursor rawQuery(String sql, String[] args) {
        readLock.lock();
        try {
            return db.rawQuery(sql, args);
        } finally {
            readLock.unlock();
        }
    }

    public void exeSql(String sql, Object[] args) {
        writeLock.lock();
        try {
            db.execSQL(sql, args);
        } finally {
            writeLock.unlock();
        }
    }

    private Collection<T> cursorToObjects(Cursor cursor) {
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
    
    private T newInstance(Cursor cursor) {
        Object object = null;
        try {
            object = claz.newInstance();
            if(id != null) {
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
        int inc = id == null ? 1 : 0;
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

            if(columnInfo.name.equals("")) {
                columnInfo.name = field.getName();
            }

            if(columnInfo.index == -1) {
                columnInfo.index = index;
                index++;
            }

            if(column.primary()) {
                primaryColumns.add(columnInfo);
            }

            if(column.ID()) {
                if(id != null ) {
                    throw new RuntimeException("ID can't be set more than one times!");
                }
                id = columnInfo;
                if(!id.type.getName().equals("INTEGER")) {
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
        
        genCreateTableSql();
        genInsertSql();
        if(debug) {
            System.out.println("createTableSql = " + createTableSql);
            System.out.println("insertSql = " + insertSql);
        }
    }

    private void genCreateTableSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
        if(id != null) {
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

        if(primaryColumns.size() > 0) {
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
        insertSql =  insertOrReplaceSql.replaceFirst("OR REPLACE ", "");
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
