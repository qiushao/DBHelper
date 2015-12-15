

DBHelper
======

DBHelper是一个轻量级的ORM数据框架，简单易用。

### 导入
#### eclipse
下载 [dbhelper-1.1.8.jar](https://github.com/qiushao/DBHelper/raw/master/downloads/dbhelper-1.1.8.jar)，将其放到工程的 `libs` 目录下.

#### gradle
在模块的构建脚本中添加如下依赖
```
compile 'net.qiushao:dbhelper:1.1.8'
```

### 基本用法
#### 1. 定义一个 Java Bean 类
```
package net.qiushao.lib;
import net.qiushao.lib.dbhelper.annotation.Column;
import net.qiushao.lib.dbhelper.annotation.Database;
@Database
public class Person {
    @Column(primary = true)
    String id;
    @Column()
    String name;
    @Column
    int age;
    @Column
    boolean marry;
    @Column
    float height;
    @Column
    double weight;

    public Person() {}

    public Person(String id, String name, int age, boolean marry, float height, double weight) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.marry = marry;
        this.height = height;
        this.weight = weight;
    }
}
```
我们用@Database注解注明这是一个数据库表信息，这个类中的哪些字段需要加入数据库表就用@Column 注解注明。

**注意：你需要提供一个无参构造函数**

#### 2. get DBHelper
```
DBHelper<Person> db;  
db = DBFactory.getInstance(context).getDBHelper(Person.class);
```

#### 3. insert, delete, update, query
- insert
```
//insert one object
db.insert(new Person("id1", "qiushao", 26, false, 165.0f, 53.0));

//insert a list
Collection<Person> persons = new LinkedList<>();
int count = 1000;
for(int i = 0; i < count; i++) {
     Person person = new Person("id" + i, "name" + i, i, false, 168.0f, 54.00);
     persons.add(person);
}
db.insertAll(persons);
```

- delete
```
db.delete("name=? and age=?", new String[]{"qiushao", String.valueOf(23)});
```

- update
DBHelper 并没有直接提供update接口，但可以通过以下两种方法达到数据库更新的目的。
```
//1.插入或更新, 如果主键已经在数据库中存在了，则更新，否则插入数据库
db.insertOrReplace(new Person(id, name, age, marry, height, weight));

//2.execSQL, 直接写数据库语句
db.execSQL("update " + db.getTableName() + " set age = 30 where id = ?", new Object[]{"1"});
```

- query
```
//条件查询，推荐使用占位符？的形式
Collection<Person> objects = db.query("name=?", new String[] {"qiushao"});

//查询所有
Collection<Person> objects = db.query(null, null);

//根据主键查询
Person person = db.queryByPrimary(new String[]{"3"});
```

### more detail 
更详细的用法请参见[DBHelper WiKi](https://github.com/qiushao/DBHelper/wiki)
