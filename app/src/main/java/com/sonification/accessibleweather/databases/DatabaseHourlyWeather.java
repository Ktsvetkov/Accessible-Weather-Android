package com.sonification.accessibleweather.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHourlyWeather extends SQLiteOpenHelper
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Utility class that defines methods to store and access the next 24 hour weather,
	 * while this application will update the database everytime the app is opened,
	 * values will only be updated when they differ from the existing values for each hour.
	 * Contains the following columns:
	 * 0 - _id
	 * 1 - hour  	  (hh)
	 * 2 - day   	  (DD)
	 * 3 - month 	  (MM)
	 * 4 - year  	  (YYYY)
	 * 5 - temp
	 * 6 - condition  (ex: clear)
	 * 7 - windSpeed
	 * 8 - feelsLikeTemp
	 * 9 - pop
	 */
	
	private static final String DATABASE_NAME = "hourlyweather.db";
	private static final int DATABASE_VERSION = 2;
	
	private static final String TABLE_NAME = "hourlyweather";
	
	// Column name and identifier                                   Column number
	public static final String _ID = "_id";							//0
	public static final String HOUR = "hour";						//1
	public static final String DAY = "day";							//2
	public static final String MONTH = "month";						//3
	public static final String YEAR = "year";						//4
	public static final String TEMP_F = "temp_f";					//5
	public static final String TEMP_C = "temp_c";					//6
	public static final String CONDITION = "condition";				//7
	public static final String WINDSPEED_F = "windspeed_f";			//8
	public static final String WINDSPEED_C = "windspeed_c";			//9
	public static final String FEELSLIKETEMP_F = "feelsliketemp_f";	//10
	public static final String FEELSLIKETEMP_C = "feelsliketemp_c";	//11
	public static final String POP = "pop";							//12


	public  DatabaseHourlyWeather(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE " + TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY, "
					+ HOUR + " TEXT, "
					+ DAY + " TEXT, "
					+ MONTH + " TEXT, "
					+ YEAR + " TEXT, "
					+ TEMP_F + " TEXT, "
					+ TEMP_C + " TEXT, "
					+ CONDITION + " TEXT, "
					+ WINDSPEED_F + " TEXT, "
					+ WINDSPEED_C + " TEXT, "
					+ FEELSLIKETEMP_F + " TEXT, "
					+ FEELSLIKETEMP_C + " TEXT, "
					+ POP + " TEXT);";
		
		db.execSQL(sql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	public void addRecord(String hour, String day, String month, String year, String temp_f, String temp_c, String condition, String windSpeed_f, String windSpeed_c , String feelsLikeTemp_f, String feelsLikeTemp_c, String pop)
	{
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(HOUR, hour);
		values.put(DAY, day);
		values.put(MONTH, month);
		values.put(YEAR, year);
		values.put(TEMP_F, temp_f);
		values.put(TEMP_C, temp_c);
		values.put(CONDITION, condition);
		values.put(WINDSPEED_F, windSpeed_f);
		values.put(WINDSPEED_C, windSpeed_c);
		values.put(FEELSLIKETEMP_F, feelsLikeTemp_f);
		values.put(FEELSLIKETEMP_C, feelsLikeTemp_c);
		values.put(POP, pop);


		db.insert(TABLE_NAME, null, values);
	}
	
	public Cursor allRows()
	{
		/* 
		 * Returns a cursor to all rows and all columns in the database in the order in which they were initialized
		 */
		String[] from = { _ID, HOUR, DAY, MONTH, YEAR, TEMP_F, TEMP_C, CONDITION, WINDSPEED_F, WINDSPEED_C, FEELSLIKETEMP_F, FEELSLIKETEMP_C, POP};
		
		String order = _ID;
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, order);
		//activity.startManagingCursor(cursor);
		return cursor;
	}
	
	public Cursor searchRows(String key, String value)
	{
		/*
		 * Returns a Cursor to all columns in the db where rows are matched by the incoming key-value pair
		 */
		String[] from = { _ID, HOUR, DAY, MONTH, YEAR, TEMP_F, TEMP_C, CONDITION, WINDSPEED_F, WINDSPEED_C, FEELSLIKETEMP_F, FEELSLIKETEMP_C, POP};
		String order = _ID;
		String where = key + " = ?";
		String[] args = new String[] {value};
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME, from, where, args, null, null, order);
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
	
	public void close()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.close();
	}
}
