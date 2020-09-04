package com.GRP.Group5.RoadTripApp.Activities.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/*
 * Created by Yi on 02/03/2018.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "place.db";
    public static final String TABLE_NAME = "place666_table";
    public static final String COL1 = "ID";
    public static final String COL2 = "NAME";
    public static final String COL3 = "LATITUDE";
    public static final String COL4 = "LONGITUDE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    //create the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, LATITUDE DOUBLE, LONGITUDE DOUBLE)";
        db.execSQL(createTable);
        //db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, LATITUDE DOUBLE, LONGITUDE DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    //insert data to the database
    public boolean insertData(String name, LatLng position){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2, name);
        contentValues.put(COL3, position.latitude);
        contentValues.put(COL4, position.longitude);

        long result  = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    //show data
    public Cursor showData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        //Log.d("Database", "data = "+ data);
        return data;
    }

    //delete data
    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }

    public Cursor selectData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT LATITUDE, LONGITUDE FROM " + TABLE_NAME, null);
        //Log.d("Database", "select data = "+ data);
        return data;
    }
}
