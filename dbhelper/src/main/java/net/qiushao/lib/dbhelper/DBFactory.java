package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.text.TextUtils;

import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class DBFactory {
	private static DBFactory instance;
	private ConcurrentHashMap<String, DBHelper> map;
	private Context context;

	public static DBFactory getInstance(Context context) {
		if (instance == null) {
			synchronized (DBFactory.class) {
				if (instance == null) {
					instance = new DBFactory(context);
				}
			}
		}
		return instance;
	}

	private DBFactory(Context context) {
		this.context = context.getApplicationContext();
		map = new ConcurrentHashMap<String, DBHelper>();
	}

	public synchronized <T> DBHelper<T> getDBHelper(Class<T> claz) {
		Database database = claz.getAnnotation(Database.class);
		if (database == null)
			return null;

		String dbName = database.databaseName();
		String tableName = database.tableName();
		if (TextUtils.isEmpty(dbName)) {
            dbName = context.getPackageName().replaceAll("\\.", "_");
		}
		if(TextUtils.isEmpty(tableName)) {
            tableName = claz.getSimpleName();
		}

        Timestamp timestamp = claz.getAnnotation(Timestamp.class);
		if (timestamp != null) {
			SimpleDateFormat df = new SimpleDateFormat(timestamp.format(), Locale.US);
			String date = df.format(new Date());
			dbName = dbName + date + ".db";
		} else {
			dbName = dbName + ".db";
		}
		
        String databaseDir = database.databaseDir();
        if(databaseDir.equals("")) {
            databaseDir = context.getDatabasePath("dbhelper").getParentFile().getAbsolutePath();
        }

        String key = databaseDir + dbName + tableName;
        if (map.containsKey(key)) {
            return map.get(key);
        }

		DBHelper<T> db = new DBHelper<>(context, databaseDir, dbName, tableName, database.tableVersion(), claz);
		map.put(key, db);
		return db;
	}
}
