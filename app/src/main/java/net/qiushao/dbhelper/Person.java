package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.Timestamp;

@Database(databaseName = "person", tableName = "person", tableVersion = 1,
        databaseDir = "/data/misc/konka/dbhelper/database")
@Timestamp
public class Person {
    @Column(index = 0, ID = true)
    private long _id;

    @Column(index = 1)
    public String name;

    @Column(index = 2)
    public int age;

    @Column(index = 3)
    public boolean marry;

    @Column(index = 4)
    public double weight;

    public Person() {}
    public Person(String name, int age, boolean marry, double weight) {
        this.name = name;
        this.age =age;
        this.marry = marry;
        this.weight = weight;
    }
}
