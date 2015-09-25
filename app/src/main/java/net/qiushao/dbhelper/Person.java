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

    public Person() {}
    public Person(String name, int age, boolean marry, double weight) {
        this.name = name;
        this.age =age;
        this.marry = marry;
        this.weight = weight;
    }
}
