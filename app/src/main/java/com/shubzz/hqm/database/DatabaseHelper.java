package com.shubzz.hqm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shubzz.hqm.adapter.Person;
import com.shubzz.hqm.utils.SessionHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "vill_db";

    private SessionHandler sessionHandler;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sessionHandler = new SessionHandler(context);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PersonDb.createTable(sessionHandler.getVill_name()));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PersonDb.TABLE_NAME);
        onCreate(db);
    }

    public long insertPerson(String name, String mno ,String age,String wfro,String aav,String ccwp,String isfc){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String table_name = ((new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).split("-")[2] + "_" + sessionHandler.getVill_name()).trim().replaceAll(" ","_");
        values.put(PersonDb.COLUMN_PERSON, name);
        values.put(PersonDb.COLUMN_AGE,age);
        values.put(PersonDb.COLUMN_MNO,mno);
        values.put(PersonDb.COLUMN_WFRO,wfro);
        values.put(PersonDb.COLUMN_AAV,aav);
        values.put(PersonDb.COLUMN_CCWP,ccwp);
        values.put(PersonDb.COLUMN_ISFC,isfc);

        long id = db.insert(table_name,null,values);

        db.close();
        return id;
    }

    public long getCount(){
        String table_name = ((new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).split("-")[2] + "_" + sessionHandler.getVill_name()).trim().replaceAll(" ","_");
        String countQuery = "SELECT  * FROM " + table_name;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        long count = cursor.getCount();
        cursor.close();
        return count;
    }
}
