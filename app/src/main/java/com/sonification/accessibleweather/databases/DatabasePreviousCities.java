package com.sonification.accessibleweather.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabasePreviousCities extends SQLiteOpenHelper
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Utility class that defines methods to store and access
	 * the previous city searches
	 * Contains the following columns:
	 * 0 - _id
	 * 1 - city name
	 * 2 - country name
	 * 3 - latitude
	 * 4 - longitude
	 */

    private static final String DATABASE_NAME = "previous_cities.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "previous_cities";

    // Column name and identifier                                   Column number
    public static final String _ID = "_id";							//0
    public static final String CITY_NAME = "city_name";			    //1
    public static final String COUNTRY_NAME = "country_name";		//2
    public static final String LATITUDE = "latitude";				//3
    public static final String LONGITUDE = "longitude";				//4

    public  DatabasePreviousCities(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + CITY_NAME + " TEXT, "
                + COUNTRY_NAME + " TEXT, "
                + LATITUDE + " TEXT, "
                + LONGITUDE + " TEXT);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addRecord(String cityName, String countryName, String latitude, String longitude)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITY_NAME, cityName);
        values.put(COUNTRY_NAME, countryName);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);

        db.insert(TABLE_NAME, null, values);
    }

    public Cursor allRows()
    {
		/*
		 * Returns a cursor to all rows and all columns in the database in the order in which they were initialized
		 */

        String[] from = { _ID, CITY_NAME, COUNTRY_NAME, LATITUDE, LONGITUDE};

        String order = _ID;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, order);
        //activity.startManagingCursor(cursor);
        return cursor;
    }

    public long count()
    {
        long count;
        SQLiteDatabase db = getReadableDatabase();
        count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return count;
    }

    public void deleteEntry(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, _ID+"="+id, null);
    }

    public void deleteAllEntries()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public Cursor searchRows(String key, String value)
    {
		/*
		 * Returns a Cursor to all columns in the db where rows are matched by the incoming key-value pair
		 */
        String[] from = { _ID, CITY_NAME, COUNTRY_NAME, LATITUDE, LONGITUDE};
        String order = _ID;
        String where = key + " = ?";
        String[] args = new String[] {value};

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, from, where, args, null, null, order);
        return cursor;
    }

    public void close()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }
}