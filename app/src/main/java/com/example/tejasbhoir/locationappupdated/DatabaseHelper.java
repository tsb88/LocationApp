package com.example.tejasbhoir.locationappupdated;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Objects;

/**
 * Created by sanjaybhoir2002 on 11/17/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Locations.db";

    public static final String TABLE_NAME = "Check_Ins";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "name";
    public static final String COL_3 = "latitude";
    public static final String COL_4 = "longitude";
    public static final String COL_5 = "time";
    public static final String COL_6 = "address";

    public static final String TABLE2_NAME = "Location_Names";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, LATITUDE DOUBLE," +
                " LONGITUDE DOUBLE, TIME TEXT, ADDRESS TEXT)");
        db.execSQL("create table " + TABLE2_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
        onCreate(db);
    }

    public boolean insertDataCheckIn(String name, double latitude, double longitude, String time, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, latitude);
        contentValues.put(COL_4, longitude);
        contentValues.put(COL_5, time);
        contentValues.put(COL_6, address);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertDataLocationName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        long result = db.insert(TABLE2_NAME, null, contentValues);
        return result != -1;
    }

    public boolean isPresent(String name) {
        Cursor result = getAllNameData();
        int bool = 0;
        if(result.getCount() > 0) {
            while (result.moveToNext()) {
                if (Objects.equals(result.getString(1), name)) {
                    bool = 1;
                    break;
                }
            }
        }
        return bool != 0;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public Cursor getAllNameData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE2_NAME, null);
    }

    public void deleteAllData () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.delete(TABLE2_NAME, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE2_NAME + "'");
    }

    public boolean updateData(String id, String name, double latitude, double longitude, String time, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, latitude);
        contentValues.put(COL_4, longitude);
        contentValues.put(COL_5, time);
        contentValues.put(COL_6, address);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] {id});
        return true;
    }

    public Cursor rowData(String select) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(select, null);
    }
}
