

DBHelper
======

DBHelper是一个轻量级的ORM数据框架，简单易用。

### 导入
#### eclipse
下载 [dbhelper-1.0.5.jar](https://github.com/qiushao/DBHelper/raw/master/downloads/dbhelper-1.0.5.jar)，将其放到工程的 `libs` 目录下.

#### gradle
在模块的构建脚本中添加如下依赖
```
compile 'net.qiushao:dbhelper:1.0.5'
```

### 基本用法
#### 1. 定义一个 Java Bean 类
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
我们用@Database注解注明这是一个数据库表信息，这个类中的哪些字段需要加入数据库表就用@Column 注解注明。
默认情况下，数据库名为应用的包名（`.`号会被替换为`_`） + ".db"， 表名为类名。
这个例子中，DBHelper产生的数据库名为`net_qiushao_dbhelper.db`，存放在`/data/data/net.qiushao.dbhelper`目录下， 自动创建数据库表的语句可能如下：
```
CREATE TABLE IF NOT EXISTS Person(name TEXT, age INTEGER, marry INTEGER, weight REAL)
```
这里说可能是因为各列的顺序是不定的。

当然我们可以指定数据库名，路径，表名，版本，列的顺序等信息：
```
package net.qiushao.dbhelper;

import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;
import net.qiushao.lib.dbhelper.annotation.Timestamp;

@Database(databaseName = "person", databaseDir = "/data/misc/konka/com.konka.systeminfo/databases",
        tableName = "person", tableVersion = 1)
@Timestamp
public class Person {
    @Column(index = 0, autoincrementID = true)
    private long _id;

    @Column(index = 1)
    public String name;

    @Column(index = 2)
    public int age;

    @Column(index = 3)
    public boolean marry;

    @Column(index = 4)
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

需要说明的有两点：
1. `@Timestamp`注解，使用这个注解后，会在数据库名后增加一个时间戳，例如"person2015-09-28.db"。可以通过 `@Timestamp(format = "yyyy-MM-dd")` 来指定格式。
2. `autoincrementID`，设置这个属性后，此列就作为数据库的主键，并且自动增加。

**注意：你需要提供一个无参构造函数**

#### 2. get DBHelper
```
DBHelper db = DBFactory.getInstance(context).getDBHelper(Person.class);
```

#### 3. insert, delete, update, query
- insert
```
    private void insert() {
        //insert one object
        db.insert(new Person("shaoqiu", 26, false, 53.0));

        //insert a list
        Collection<Object> persons = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            Person person = new Person("name" + i, i, false, 52.0);
            persons.add(person);
        }
        db.insertAll(persons);
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
