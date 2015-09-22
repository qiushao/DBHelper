package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.Table;

/**
 * Created by shaoqiu on 2015-9-22.
 */
@Database(name = "person")
@Table(name = "person")
public class Person {
    @Column(index = 1)
    public String name;
    @Column(index = 2)
    public int age;
    @Column(index = 3)
    public boolean marry;
    @Column(index = 4)
    public double weight;

    public Person() {
        name = "";
        age = 0;
        marry = false;
        weight = 0.0;
    }
}
