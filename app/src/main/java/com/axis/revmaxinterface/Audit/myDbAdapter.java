/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class myDbAdapter {
    myDbHelper myhelper;
    public String znumber, currency;
    public int cid;
    public myDbAdapter(Context context)
    {
        myhelper = new myDbHelper(context);
    }

    public long insertData(String znumber, String currency, String netammount, String taxamount, String vatrate, String date, String time)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.ZNUMBER, znumber);
        contentValues.put(myDbHelper.CURRENCY, currency);
        contentValues.put(myDbHelper.NETTAMOUNT, netammount);
        contentValues.put(myDbHelper.TAXAMOUNT, taxamount);
        contentValues.put(myDbHelper.VATRATE, vatrate);
        contentValues.put(myDbHelper.DATE, date);
        contentValues.put(myDbHelper.TIME, time);
        long id = dbb.insert(myDbHelper.TABLE_NAME, null , contentValues);
        return id;
    }



//    public String getData()
//    {
//        SQLiteDatabase db = myhelper.getWritableDatabase();
//        String[] columns = {myDbHelper.UID,myDbHelper.ZNUMBER,myDbHelper.CURRENCY,myDbHelper.NETTAMOUNT,myDbHelper.TAXAMOUNT,myDbHelper.VATRATE,myDbHelper.DATE,myDbHelper.TIME};
//        Cursor cursor =db.query(myDbHelper.TABLE_NAME,columns,null,null,null,null,null);
//        StringBuffer buffer= new StringBuffer();
//        while (cursor.moveToNext())
//        {
//             cid =cursor.getInt(cursor.getColumnIndex(myDbHelper.UID));
//             znumber =cursor.getString(cursor.getColumnIndex(myDbHelper.ZNUMBER));
//             currency =cursor.getString(cursor.getColumnIndex(myDbHelper.CURRENCY));
////            buffer.append(cid+ "   " + znumber + "   " + currency +" \n");
//        }
//        return buffer.toString();
//    }

//    public  int delete(String uname)
//    {
//        SQLiteDatabase db = myhelper.getWritableDatabase();
//        String[] whereArgs ={uname};
//
//        int count =db.delete(myDbHelper.TABLE_NAME ,myDbHelper.NAME+" = ?",whereArgs);
//        return  count;
//    }
//
//    public int updateName(String oldName , String newName)
//    {
//        SQLiteDatabase db = myhelper.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(myDbHelper.NAME,newName);
//        String[] whereArgs= {oldName};
//        int count =db.update(myDbHelper.TABLE_NAME,contentValues, myDbHelper.NAME+" = ?",whereArgs );
//        return count;
//    }

    static class myDbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "revAudit";    // Database Name
        private static final String TABLE_NAME = "zreports";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String ZNUMBER = "ZNUMBER";    //Column II
        private static final String CURRENCY= "CURRENCY";    // Column III
        private static final String NETTAMOUNT = "NETTAMOUNT";    //Column IV
        private static final String TAXAMOUNT= "TAXAMOUNT";    // Column V
        private static final String VATRATE = "VATRATE";    //Column VI
        private static final String DATE= "DATE";    // Column VII
        private static final String TIME= "TIME";    // Column IIX
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ZNUMBER+" VARCHAR(255) ,"+ CURRENCY+" VARCHAR(225)," +
                NETTAMOUNT+" VARCHAR(255) ,"+ TAXAMOUNT+" VARCHAR(225),"+VATRATE+" VARCHAR(255) ,"+ DATE+" VARCHAR(225),"
                +TIME+" VARCHAR(225));";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private Context context;

        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) {
                Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Message.message(context,"OnUpgrade");
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }catch (Exception e) {
                Message.message(context,""+e);
            }
        }
    }

}