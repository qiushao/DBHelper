package net.qiushao.lib;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;

/**
 * Created by qiushao on 15-11-29.
 */
@Database(tableVersion = 3)
public class Record {

    @Column(ID = true)
    private long id;
    @Column
    public float money;
    @Column
    public String date;
    @Column
    public String note;

    public Record() {

    }

    public Record(String date, float money, String note) {
        this.date = date;
        this.money = money;
        this.note = note;
    }
}
