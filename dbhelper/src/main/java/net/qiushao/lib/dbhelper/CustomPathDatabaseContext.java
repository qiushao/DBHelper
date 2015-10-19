package net.qiushao.lib.dbhelper;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.io.File;
import java.io.IOException;

class CustomPathDatabaseContext extends ContextWrapper {

	private String mDirPath;
    private boolean isPublic;

	public CustomPathDatabaseContext(Context base, String dir, boolean isPublic) {
		super(base);
		mDirPath = dir;
        this.isPublic = isPublic;
	}

	@Override
	public File getDatabasePath(String name) {
		File result = new File(mDirPath + File.separator + name);
		if (!result.getParentFile().exists()) {
			result.getParentFile().mkdirs();
            if(isPublic) {
                try {
                    Runtime.getRuntime().exec("chmod -R 777 " + result.getParent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		return result;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase db = super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory, errorHandler);
        if(isPublic) {
            try {
                Runtime.getRuntime().exec("chmod -R 777 " + getDatabasePath(name).getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return db;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory) {
        SQLiteDatabase db = super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
        if(isPublic) {
            try {
                Runtime.getRuntime().exec("chmod -R 777 " + getDatabasePath(name).getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return db;
	}
}
