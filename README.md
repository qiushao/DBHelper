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

以上是最简单的用法，没有定义任何约束条件，如果需要定义一些条件约束，请继续往下看接口说明。
### 注解
默认情况下，不需要使用任何注解，但有时候需要指定一些信息，可以通过以下注解来实现。
```
@Database(version = 2)
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
```

#### @Database （类注解）
数据库位置在` /data/data/[package]/databases`，数据库名为类的包名加类名，数据库表名为类名。例如 `net.qiushao.dbhelper.User` 类生成的数据库名为`net_qiushao_dbhelper_User`，表名为`User`。以上这些信息是固定不变的。用户只可以指定数据库的版本信息：
- version ：指定数据库版本，默认版本为1。当数据库表结构有变化时，需要增加数据库的版本。

#### @ID （成员变量注解）
指定数据库自增长字段， 一个数据库表中只可以有一个ID字段。在插入数据时，这个字段是不需要赋值的，由数据库系统自动赋值。

#### @Unique （成员变量注解）
指定某列的值是否可以重复

#### transient 
这个不是注解，而java自带的关键字，使用此关键字修饰的变量不会保存到数据库。

#### @Primary （成员变量注解）
这个注解在上面的例子中没有使用到，因为会跟@ID注解有冲突。本质上两者都是指定数据库主键，区别是：
- @ID注解的列为数据库主键，且值会自动增长，不需要赋值，但一个表中只能有一个ID。
- @Primary注解的列也为数据库主键，但一个表中可以有多列同时被@Primary注解， 作为组合主键。

### DBHelper所有接口
数据库的操作都是通过`DBHelper`类来实现的。提供的接口如下：
#### 增
- public void insert(T object) ：插入单个元素
- public void insertOrReplace(T object) ：在定义了数据库主键的情况下，如果该主键已存在数据库中，则只是更新数据库中该主键所对应的数据，如果不存在该主键，则插入数据库。
- public void insertOrIgnore(T object) ：在定义了数据库主键的情况下，如果该主键已存在数据库中，则该数据被抛弃，如果不存在该主键，则插入数据库。
- public void insertAll(Collection<T> objects) ：插入一个列表。

#### 删
- public void delete(String whereClause, Object[] whereArgs)：根据条件删除。
例：单条件删除：`db.delete("name=?", new Object[]{user.name})`，组合条件删除：`db.delete("name=? and age>?", new Object[]{user.name, user.age})`
- public void clean() ：清空数据库表，自增加字段归0

#### 改
- public int update(ContentValues values, String whereClause, String[] whereArgs) ：这种形式最灵活，可以满足所有的更新操作，把需要更新的字段放到 ContentValues  中。

- public void insertOrReplace(T object) ：需要数据库表定义了主键，如果主键重复则为更新。

#### 查
- public List`<T>` query(String whereClause, String[] args) ：条件查询，如果传入的两个参数都是 `null`，则返回所有的数据。

- public Cursor rawQuery(String sql, String[] args) ： 执行用户传入的完整查询语句，返回Cursor形式的查询结果。

- public long size() ： 返回数据库的行数

#### 执行不带返回值的数据库语句
- public void execSQL(String sql)
- public void execSQL(String sql, Object[] args)
