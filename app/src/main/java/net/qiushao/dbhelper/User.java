package net.qiushao.dbhelper;

public class User {
    String name;
    int age;

    public User(){}
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "user(" + name + ", " + age + ")";
    }
}
