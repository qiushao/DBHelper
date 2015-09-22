package net.qiushao.dbhelper;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.qiushao.lib.dbhelper.DBFactory;
import net.qiushao.lib.dbhelper.DBHelper;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.insert).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.query).setOnClickListener(this);

        db = DBFactory.getInstance(this).getDBHelper(Person.class);
        db = DBFactory.getInstance(this).getDBHelper(Person.class, "/data/database");
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
}
