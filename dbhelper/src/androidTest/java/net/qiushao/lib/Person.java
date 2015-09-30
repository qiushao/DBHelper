package net.qiushao.lib;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;

/**
 * Created by shaoqiu on 2015-9-30.
 */
@Database(tableVersion = 2)
public class Person {
    @Column(primary = true)
    String id;

    @Column()
    String name;

    @Column
    int age;

    @Column
    boolean marry;

    @Column
    float height;

    @Column
    double weight;

    public Person() {}

    public Person(String id, String name, int age, boolean marry, float height, double weight) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.marry = marry;
        this.height = height;
        this.weight = weight;
    }
}
