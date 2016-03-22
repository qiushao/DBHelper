package net.qiushao.lib.dbhelper;

import java.lang.reflect.Field;


/**
 * Created by shaoqiu on 15-8-29.
 */
class ColumnInfo {
    public String name = "";
    public DBType type = null;
    public int index = 0;
    public Field field = null;
    public boolean isID = false;
    public boolean isUnique = false;

    public ColumnInfo(Field field, String name, DBType type) {
        this.field = field;
        this.name = name;
        this.type = type;
    }
}
