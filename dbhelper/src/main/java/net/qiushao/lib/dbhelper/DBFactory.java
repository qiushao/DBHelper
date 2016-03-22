package net.qiushao.lib.dbhelper;

import android.content.Context;

import net.qiushao.lib.dbhelper.annotation.Database;

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
        String dbName = claz.getName().replaceAll("\\.", "_");
        if (map.containsKey(dbName)) {
            return map.get(dbName);
        }

        int version = 1;
        Database database = claz.getAnnotation(Database.class);
        if (database != null) {
            version = database.version();
        }
		DBHelper<T> db = new DBHelper<T>(context, claz, dbName + ".db", version);
		map.put(dbName, db);
		return db;
	}
}
