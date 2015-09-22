

DBHelper
======

an light weight android orm framwork

### import library
#### eclipse
download the library [dbhelper-1.0.2-sources.jar](https://bintray.com/artifact/download/qiushao/maven/net/qiushao/dbhelper/1.0.2/dbhelper-1.0.2-sources.jar)，put it into your project's `libs` directory.

#### gradle
add this dependency in your module build.gradle file
```
compile 'net.qiushao:dbhelper:1.0.2'
```

### example

#### 1. define a java bean with Database Annotation, this java bean will be a table
```
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

```

#### 2. insert, delete, update, query
```
DBHelper db;
//database will be stored at /data/data/package/database
db = DBFactory.getInstance(this).getDBHelper(Person.class);
//or you can assign the database dir path
db = DBFactory.getInstance(this).getDBHelper(Person.class, "/data/database");
...
private void insert() {
        Person person = new Person();
        person.name = "qiushao";
        person.age = 26;
        person.marry = false;
        person.weight = 54.0;
        db.insert(person);
    }

    private void delete() {
        db.delete("name=?", new Object[] {"qiushao"});
        db.clean();
    }

    private void update() {
        Person person = new Person();
        person.name = "qiushao";
        person.age = 28;
        person.marry = true;
        person.weight = 55.0;
        db.update("name=?", new Object[] {"qiushao"}, person);
    }

    private void query() {
        Collection<Object> persons = db.query(null, null);
        for(Object object:persons) {
            Person person = (Person) object;
            System.out.println("name = " + person.name);
            System.out.println("age = " + person.age);
            System.out.println("marry = " + person.marry);
            System.out.println("weight = " + person.weight);
        }
    }
```
