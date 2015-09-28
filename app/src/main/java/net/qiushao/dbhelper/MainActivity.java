package net.qiushao.dbhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import net.qiushao.lib.dbhelper.DBFactory;
import net.qiushao.lib.dbhelper.DBHelper;

import java.util.Collection;
import java.util.LinkedList;

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
        myRegisterReceiver();
    }

    private void myRegisterReceiver(){
        MyVolumeReceiver mVolumeReceiver = new MyVolumeReceiver() ;
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.media.MASTER_VOLUME_CHANGED_ACTION") ;
        registerReceiver(mVolumeReceiver, filter) ;
    }

    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
                Toast.makeText(getApplicationContext(), "volume change", Toast.LENGTH_SHORT).show();
            }
        }
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
        //insert one object
        db.insert(new Person("shaoqiu", 26, false, 53.0));

        //insert a list
        Collection<Object> persons = new LinkedList<>();
        for(int i = 0; i < 100; i++) {
            Person person = new Person("name" + i, i, false, 52.0);
            persons.add(person);
        }
        db.insertAll(persons);
    }

    private void delete() {
        //delete by condition where name = shaoqiu
        db.delete("name=?", new Object[] {"shaoqiu"});
        //delete all data from table Person
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
