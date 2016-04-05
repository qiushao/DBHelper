package net.qiushao.dbhelper;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import net.qiushao.lib.dbhelper.DBFactory;
import net.qiushao.lib.dbhelper.DBHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.insert).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.clean).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.query).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.insert:
                insert();
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.clean:
                clean();
                break;
            case R.id.update:
                update();
                break;
            case R.id.query:
                query();
                break;
            default:
                break;
        }
    }

    private void insert() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        User user = new User();
        user.name = "qiushao";
        user.age = 27;
        user.email = "qiushaox@gmail.com";
        user.foobar = 222;
        udb.insert(user);

        user.name = "shaoqiu";
        user.email = "shaoqiu@gmail.com";
        udb.insert(user);

        user.name = "shaoqiu";
        user.age = 30;
        user.email = "360325900@qq.com";
        udb.insert(user);
    }

    private void delete() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        udb.delete("name=? and age>?", new Object[]{"qiushao", 100});
    }

    private void clean() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        udb.clean();
    }

    private void update() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        ContentValues contentValues = new ContentValues();
        contentValues.put("age", 100);
        udb.update(contentValues, "name=?", new String[]{"shaoqiu"});

        List<User> users = udb.query("email=?", new String[]{"qiushaox@gmail.com"});
        for(User user : users) {
            user.age = 200;
            udb.insertOrReplace(user);
        }
    }

    private void query() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        List<User> users = udb.query("name=?", new String[]{"shaoqiu"});
        for (User user : users) {
            Log.d("qiushao", user.toString());
        }
    }
}
