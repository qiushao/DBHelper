package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Table;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBHelper extends SQLiteOpenHelper{

	private Class<?> claz;
    private SQLiteDatabase db;
    private SQLiteStatement insertStatement;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private String dbName;
    private String tableName;
    private int tableVersion;
    private String createTableSql;
    private String insertSql;
    private LinkedList<ColumnInfo> columns;
    
	public DBHelper(Context context, String dir, String name, int version, Class<?> claz) {
		super(new CustomPathDatabaseContext(context, dir), name, null, version);
		dbName = name;
        tableVersion = version;
        this.claz = claz;
        initDatabaseInfo();
        db = getWritableDatabase();
        db.execSQL(createTableSql);
        insertStatement = db.compileStatement(insertSql);
	}
	
	public void insert(Object object) {
        writeLock.lock();
        try {
            bindInsertStatementArgs(object);
            insertStatement.executeInsert();
        } finally {
            writeLock.unlock();
        }
    }

    public void insert(Collection<Object> objects) {
        writeLock.lock();
        db.beginTransaction();
        try {
            for (Object object : objects) {
                bindInsertStatementArgs(object);
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
        db.execSQL(sql.toString(), whereArgs);
    }

    public void clean() {
        db.execSQL("delete from " + tableName);
    }
    
    public void update(String whereClause, Object[] whereArgs, Object object) {
        StringBuilder sql = new StringBuilder();
        sql.append("update ");
        sql.append(tableName);
        sql.append(" set ");

        int bindArgsSize = whereArgs.length + columns.size();
        Object[] bindArgs = new Object[bindArgsSize];
        int index = 0;

        try {
            for (ColumnInfo column : columns) {
                sql.append(column.name);
                sql.append("=?,");
                bindArgs[index++] = column.field.get(object);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        sql.deleteCharAt(sql.length() - 1);

        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" where ");
            sql.append(whereClause);
        }

        for (Object arg : whereArgs) {
            bindArgs[index++] = arg;
        }

        db.execSQL(sql.toString(), bindArgs);
    }

    public Collection<Object> query(String whereClause, String[] args) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(tableName);
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        Cursor cursor = db.rawQuery(sql.toString(), args);
        return cursorToObjects(cursor);
    }

    public void exeSql(String sql, Object[] args) {
        db.execSQL(sql, args);
    }

    public Cursor rawQuery(String sql, String[] args) {
        return db.rawQuery(sql, args);
    }

    private Collection<Object> cursorToObjects(Cursor cursor) {
        LinkedList<Object> list = new LinkedList<Object>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Object object = newInstance(cursor);
                if (object != null) {
                    list.add(object);
                }
            }
        }
        cursor.close();
        return list;
    }
    
    private Object newInstance(Cursor cursor) {
        Object object = null;
        try {
            object = claz.newInstance();
            for (ColumnInfo column : columns) {
                column.field.set(object, column.type.getValue(cursor, column.index));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return object;
    }
    
    private void bindInsertStatementArgs(Object object) {
        try {
            for (ColumnInfo column : columns) {
                column.type.bindArg(insertStatement, column.index, column.field.get(object));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
	private void initDatabaseInfo() {
		Table table = claz.getAnnotation(Table.class);
        tableName = table.name();
        columns = new LinkedList<ColumnInfo>();

        Field[] fields = claz.getDeclaredFields();
        for (Field field : fields) {
            if (!DBType.isSupportType(field.getType())) continue;
            Column column = field.getAnnotation(Column.class);
            if (column == null) continue;

            field.setAccessible(true);
            ColumnInfo columnInfo = new ColumnInfo(
                    field,
                    field.getName(),
                    DBType.getDBType(field.getType()),
                    column.index()
            );
            columns.add(columnInfo);
        }
        
        Collections.sort(columns, new Comparator<ColumnInfo>() {
			@Override
			public int compare(ColumnInfo lhs, ColumnInfo rhs) {
				return lhs.index - rhs.index;
			}
		});
        
        createTableSql = genCreateTableSql();
        insertSql = genInsertSql();
    }

    private String genCreateTableSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
        sql.append("(_id integer primary key autoincrement, ");

        for (ColumnInfo column : columns) {
            sql.append(column.name);
            sql.append(" ");
            sql.append(column.type.getName());
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");

        return sql.toString();
    }

    private String genInsertSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
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
        return sql.toString();
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
