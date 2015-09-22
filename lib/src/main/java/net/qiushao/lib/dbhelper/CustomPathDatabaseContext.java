package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.io.File;

public class CustomPathDatabaseContext extends ContextWrapper {

	private String mDirPath;

	public CustomPathDatabaseContext(Context base, String dir) {
		super(base);
		mDirPath = dir;
	}

	@Override
	public File getDatabasePath(String name) {
		File result = new File(mDirPath + File.separator + name);
		if (!result.getParentFile().exists()) {
			result.getParentFile().mkdirs();
		}
		return result;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
		return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory, errorHandler);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory) {
		return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
	}
}
