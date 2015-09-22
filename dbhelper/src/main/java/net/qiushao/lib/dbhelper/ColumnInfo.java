package net.qiushao.lib.dbhelper;

import java.lang.reflect.Field;


/**
 * Created by shaoqiu on 15-8-29.
 */
public class ColumnInfo {
    public String name = "";
    public DBType type = null;
    public int index = 0;
    public Field field = null;

    public ColumnInfo(Field field, String name, DBType type, int index) {
        this.field = field;
        this.name = name;
        this.type = type;
        this.index = index;
    }
}
