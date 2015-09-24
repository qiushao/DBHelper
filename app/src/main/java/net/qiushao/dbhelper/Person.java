package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;

@Database
public class Person {
    @Column
    public String name;
    @Column
    public int age;
    @Column
    public boolean marry;
    @Column
    public double weight;

    public Person() {
        name = "";
        age = 0;
        marry = false;
        weight = 0.0;
    }
}
