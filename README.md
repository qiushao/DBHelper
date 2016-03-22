DBHelper
======

DBHelper是一个轻量级的ORM数据框架，简单易用。

### 导入
#### eclipse
下载 [dbhelper-2.0.0.jar](https://github.com/qiushao/DBHelper/raw/master/downloads/dbhelper-2.0.0.jar)，将其放到工程的 `libs` 目录下.

#### gradle
在模块的构建脚本中添加如下依赖
```
compile 'net.qiushao:dbhelper:2.0.0'
```

### 基本用法
#### 1. 定义一个 Java Bean 类
```
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
```

**注意：你需要提供一个无参构造函数**

#### 2. 获取数据库辅助类实例
```
DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
```

#### 3. 通过数据库辅助类操作数据库
- insert
```
udb.insert(new User(name, age));
```

- delete
```
//根据条件删除
udb.delete("name=?", new String[]{name});
//清空数据库
udb.clean();
```

- update 更新相对麻烦一点
```
ContentValues contentValues = new ContentValues();
contentValues.put("age", 100);
udb.update(contentValues, "name=?", new String[]{"user0"});
```

- query
```
//返回所有数据
List<User> users = udb.query(null, null);
//条件查询
List<User> users = udb.query("name=?", new String[]{"qiushao"});
```

### more detail 
更详细的用法请参见[DBHelper WiKi](https://github.com/qiushao/DBHelper/wiki)

