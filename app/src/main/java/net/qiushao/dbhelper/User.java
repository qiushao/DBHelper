package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.ID;

public class User {
    @ID
    public int _id;
    public String name;
    public User(){}
}
