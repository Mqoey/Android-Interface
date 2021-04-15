/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tutlane on 06-01-2018.
 */

public class DbHandler extends SQLiteOpenHelper {
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

    public DbHandler(Context context){
        super(context,DATABASE_NAME, null, DATABASE_Version);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ZNUMBER+" VARCHAR(255) ,"+ CURRENCY+" VARCHAR(225)," +
                NETTAMOUNT+" VARCHAR(255) ,"+ TAXAMOUNT+" VARCHAR(225),"+VATRATE+" VARCHAR(255) ,"+ DATE+" VARCHAR(225),"
                +TIME+" VARCHAR(225));";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }
    // **** CRUD (Create, Read, Update, Delete) Operations ***** //

    // Adding new User Details
    void insertUserDetails(String znumber, String currency, String netammount, String taxamount, String vatrate, String date, String time){
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(ZNUMBER, znumber);
        cValues.put(CURRENCY, currency);
        cValues.put(NETTAMOUNT, netammount);
        cValues.put(TAXAMOUNT, taxamount);
        cValues.put(VATRATE, vatrate);
        cValues.put(DATE, date);
        cValues.put(TIME, time);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME,null, cValues);
        db.close();
    }
    // Get User Details
    public ArrayList<HashMap<String, String>> getZReports(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT ZNUMBER, CURRENCY, NETTAMOUNT, TAXAMOUNT, VATRATE, DATE, TIME FROM "+ TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            HashMap<String,String> user = new HashMap<>();
            user.put("znumber",cursor.getString(cursor.getColumnIndex(ZNUMBER)));
            user.put("currency",cursor.getString(cursor.getColumnIndex(CURRENCY)));
            user.put("netammount",cursor.getString(cursor.getColumnIndex(NETTAMOUNT)));
            user.put("taxamount",cursor.getString(cursor.getColumnIndex(TAXAMOUNT)));
            user.put("vatrate",cursor.getString(cursor.getColumnIndex(VATRATE)));
            user.put("date",cursor.getString(cursor.getColumnIndex(DATE)));
            user.put("time",cursor.getString(cursor.getColumnIndex(TIME)));
            userList.add(user);
        }
        return  userList;
    }

    // Get User Details based on userid
    public ArrayList<HashMap<String, String>> GetUserByUserId(int userid){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT name, location, designation FROM "+ TABLE_NAME;
        Cursor cursor = db.query(TABLE_NAME, new String[]{ZNUMBER, CURRENCY, NETTAMOUNT,TAXAMOUNT,VATRATE,DATE,TIME}, UID+ "=?",new String[]{String.valueOf(userid)},null, null, null, null);
        if (cursor.moveToNext()){
            HashMap<String,String> user = new HashMap<>();
            user.put("znumber",cursor.getString(cursor.getColumnIndex(ZNUMBER)));
            user.put("currency",cursor.getString(cursor.getColumnIndex(CURRENCY)));
            user.put("netammount",cursor.getString(cursor.getColumnIndex(NETTAMOUNT)));
            user.put("taxamount",cursor.getString(cursor.getColumnIndex(TAXAMOUNT)));
            user.put("vatrate",cursor.getString(cursor.getColumnIndex(VATRATE)));
            user.put("date",cursor.getString(cursor.getColumnIndex(DATE)));
            user.put("time",cursor.getString(cursor.getColumnIndex(TIME)));
            userList.add(user);
        }
        return  userList;
    }
}