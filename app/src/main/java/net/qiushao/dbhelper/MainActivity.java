package net.qiushao.dbhelper;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.qiushao.lib.dbhelper.DBFactory;
import net.qiushao.lib.dbhelper.DBHelper;
import net.qiushao.lib.dbhelper.Debug;

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
        user._id = 3;
        user.name = "qiushao";
        udb.insert(user);

        user.name = "shaoqiu";
        user._id = 8;
        udb.insert(user);

        user.name = "junjun";
        user._id = 9;
        udb.insert(user);
    }

    private void delete() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        udb.delete("name=?", new Object[]{"qiushao"});
    }

    private void clean() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        udb.clean();
    }

    private void update() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", "xushaoqiu");
        udb.update(contentValues, "name=?", new String[]{"shaoqiu"});
    }

    private void query() {
        DBHelper<User> udb = DBFactory.getInstance(this).getDBHelper(User.class);
        List<User> users = udb.query("name=?", new String[]{"qiushao"});
        for (User user : users) {
            Debug.d(user.name);
            user.name = "foobar";
            udb.insertOrReplace(user);
        }
    }
}
