package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.ID;
import net.qiushao.lib.dbhelper.annotation.Unique;

@Database(version = 5)
public class User {
    @ID
    public String _id;
    public String name;
    public int age;
    @Unique
    public String email;
    transient public int foobar;
    public User(){}
}
