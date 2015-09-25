

DBHelper
======

an light weight android orm framwork

### import library
#### eclipse
download the library [dbhelper-1.0.4.jar](https://github.com/qiushao/DBHelper/raw/master/downloads/dbhelper-1.0.4.jar)，put it into your project's `libs` directory.

#### gradle
add this dependency in your module build.gradle file
```
compile 'net.qiushao:dbhelper:1.0.4'
```

### base useage
#### 1. define a java bean annotation by @Database :
```
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
```
by default database name is package name, table name is class simple name, column name is field name. create table sql may be like this:`CREATE TABLE IF NOT EXISTS Person(name TEXT, age INTEGER, marry INTEGER, weight REAL)`. but column index may be different.
**NOTE: this java bean must have a no argument constructor**

#### 2. get DBHelper
```
DBHelper db = DBFactory.getInstance(context).getDBHelper(Person.class);
```

DBHelper will auto create database at `/data/data/package/database`, and create table.

#### 3. insert, delete, update, query
- insert
```
    private void insert() {
        //insert one object
        db.insert(new Person("shaoqiu", 26, false, 53.0));
        
        //insert a list
        Collection<Person> persons = new LinkedList<>();
        for(int i = 0; i < 100; i++) {
            Person person = new Person("name" + i, i, false, 52.0);
            persons.add(person);
        }
        db.insert(persons);
    }
```

- delete
```
    private void delete() {
        //delete by condition where name = shaoqiu
        db.delete("name=?", new Object[] {"shaoqiu"});
        //delete all data from table Person
        db.clean();
    }
```

- update
```

```

- query
```

```

### more 
