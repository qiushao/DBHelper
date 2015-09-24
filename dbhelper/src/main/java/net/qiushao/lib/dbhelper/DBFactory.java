package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.text.TextUtils;

import net.qiushao.lib.dbhelper.annotation.Database;

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

	public synchronized DBHelper getDBHelper(Class<?> claz) {
		return getDBHelper(claz, null);
	}

	public synchronized DBHelper getDBHelper(Class<?> claz, String databaseDir) {
		Database database = claz.getAnnotation(Database.class);
		if (database == null)
			return null;

		String dbName = database.databaseName();
		String tableName = database.tableName();
		if (TextUtils.isEmpty(dbName)) {
            dbName = context.getPackageName().replaceAll(".", "_");
		}
		if(TextUtils.isEmpty(tableName)) {
            tableName = claz.getSimpleName();
		}

		if (database.timestamp()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String date = df.format(new Date());
			dbName = dbName + date + ".db";
		} else {
			dbName = dbName + ".db";
		}
		
		String key = dbName + tableName;
		if (map.containsKey(key)) {
			return map.get(key);
		}

        if(databaseDir == null) {
            databaseDir = context.getDatabasePath("dbhelper").getParentFile().getAbsolutePath();
        }
		DBHelper db = new DBHelper(context, databaseDir, dbName, tableName, database.tableVersion(), claz);
		map.put(key, db);
		return db;
	}
}
