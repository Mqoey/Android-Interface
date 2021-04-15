/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.axis.revmaxinterface.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MainDb extends AppCompatActivity {
    EditText Name, Pass , updateold, updatenew, delete;
//    myDbAdapter helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main2);
//        Name= (EditText) findViewById(R.id.editName);
//        Pass= (EditText) findViewById(R.id.editPass);
//        updateold= (EditText) findViewById(R.id.editText3);
//        updatenew= (EditText) findViewById(R.id.editText5);
//        delete = (EditText) findViewById(R.id.editText6);

//        helper = new myDbAdapter(this);
        DbHandler db = new DbHandler(this);
        ArrayList<HashMap<String, String>> userList = db.getZReports();
        ListView lv = (ListView) findViewById(R.id.user_list);
        ListAdapter adapter = new SimpleAdapter(MainDb.this, userList, R.layout.list_row,new String[]{"znumber","currency","date"}, new int[]{R.id.name, R.id.designation, R.id.location});
        lv.setAdapter(adapter);
    }
//    public void addUser(View view)
//    {
//            long id = helper.insertData("56",
//                    "ZWL",
//                    "234",
//                    "34",
//                    "0.145",
//                    "04-04-2021",
//                    "0978");
//            if(id<=0)
//            {
//                Message.message(getApplicationContext(),"Insertion Unsuccessful");
//                Name.setText("");
//                Pass.setText("");
//            } else
//            {
//                Message.message(getApplicationContext(),"Insertion Successful");
//                Name.setText("");
//                Pass.setText("");
//            }
//        }


//    public void viewdata(View view)
//    {
//        String data = helper.getData();
//        Message.message(this,data);
//    }
//
//    public void update( View view)
//    {
//        String u1 = updateold.getText().toString();
//        String u2 = updatenew.getText().toString();
//        if(u1.isEmpty() || u2.isEmpty())
//        {
//            Message.message(getApplicationContext(),"Enter Data");
//        }
//        else
//        {
//            int a= helper.updateName( u1, u2);
//            if(a<=0)
//            {
//                Message.message(getApplicationContext(),"Unsuccessful");
//                updateold.setText("");
//                updatenew.setText("");
//            } else {
//                Message.message(getApplicationContext(),"Updated");
//                updateold.setText("");
//                updatenew.setText("");
//            }
//        }
//
//    }
//    public void delete( View view)
//    {
//        String uname = delete.getText().toString();
//        if(uname.isEmpty())
//        {
//            Message.message(getApplicationContext(),"Enter Data");
//        }
//        else{
//            int a= helper.delete(uname);
//            if(a<=0)
//            {
//                Message.message(getApplicationContext(),"Unsuccessful");
//                delete.setText("");
//            }
//            else
//            {
//                Message.message(this, "DELETED");
//                delete.setText("");
//            }
//        }
//    }
}
