package net.qiushao.lib.dbhelper;

import android.util.Log;

/**
 * Created by shaoqiu on 2016-4-8.
 */
public class Debug {
    static public boolean debug = true;
    static public String TAG = "dbhelper";
    static public void d(Object msg) {
        if (debug) {
            Log.d(TAG, msg.toString());
        }
    }
}
