package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.ID;
import net.qiushao.lib.dbhelper.annotation.Unique;

/**
 * Created by shaoqiu on 2016-3-17.
 */
@Database(version = 3)
public class User {
    @ID
    long _id;
    @Unique
    String name;
    int age;

    public User(){}
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "user(" + _id + ", " + name + ", " + age + ")";
    }
}
