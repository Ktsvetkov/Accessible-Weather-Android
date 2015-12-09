package com.sonification.accessibleweather.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseCachedWeather extends SQLiteOpenHelper
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Class that defines utility methods to access, modify and edit the weather cache
	 * Once cache entry will be made for each city. During maintenance, any cache older than 10 days will be deleted
	 * The database defined by this class has the following columns:
	 * 0 - _id
	 * 1 - date          (YYYYMMDDhhmm)
	 * 2 - lat
	 * 3 - lon
	 * 4 - city
	 * 5 - state
	 * 6 - country
	 * 7 - stationid
	 * 8 - weather       (cloud conditions)
	 * 9 - tempf
	 * 10 - tempc
	 * 11 - humidity     (Percentage humidity)
	 * 12 - windstring   (Wind description)
	 * 13 - winddir
	 * 14 - windmph
	 * 15 - windkph
	 * 16 - feelslikef
	 * 17 - feelslikec
	 * 18 - visibilitymi
	 * 19 - visibilitykm
	 * 20 - popday       (Probability of precipitation)
	 * 21 - popnight   
	 * 22 - precipin     (Precipitation in inches)
	 * 23 - precipmm     (Precipitation in millimeters)
	 * 24 - descday		 (Description of day weather)
	 * 25 - descnight	 (Description of night weather)
	 * 26 - sunrise      (HHMM)
	 * 27 - sunset       (HHMM)
	 */
	
	private static final String DATABASE_NAME = "weather_cache.db";
	private static final int DATABASE_VERSION = 2;
    /*
    Change log:
    Version 2 - Added Column 26 and 27 for sunrise and sunset
     */
	
	private static final String TABLE_NAME = "cache";
	
	// Column name and identifier                                   Column number
	public static final String _ID = "_id";							//0
	public static final String DATE = "date";						//1
	public static final String LAT = "latitude";					//2
	public static final String LON = "longitude";					//3
	public static final String CITY = "city";						//4
	public static final String STATE = "state";						//5
	public static final String COUNTRY = "country";					//6
	public static final String STATIONID = "stationid";				//7
	public static final String WEATHER = "weather";					//8
	public static final String TEMPF = "tempf";						//9
	public static final String TEMPC = "tempc";						//10
	public static final String HUMIDITY = "humidity";				//11
	public static final String WINDSTRING = "windstring";			//12
	public static final String WINDDIR = "winddir";					//13
	public static final String WINDMPH = "windmph";					//14
	public static final String WINDKPH = "windkph";					//15
	public static final String FEELSLIKEF = "feelslikef";			//16
	public static final String FEELSLIKEC = "feelslikec";			//17
	public static final String VISIBILITYMI = "visibilitymi";		//18
	public static final String VISIBILITYKM = "visibilitykm";		//19
	public static final String POPDAY = "popday";					//20
	public static final String POPNIGHT = "popnight";				//21
	public static final String PRECIPIN = "precipin";				//22
	public static final String PRECIPMM = "precipmm";				//23
	public static final String DESCDAY = "descday";					//24
	public static final String DESCNIGHT = "descnight";				//25
    public static final String SUNRISE = "sunrise";                 //26
    public static final String SUNSET = "sunset";                   //27
	
	public  DatabaseCachedWeather(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE " + TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY, "
					+ DATE + " TEXT, "
					+ LAT + " TEXT, "
					+ LON + " TEXT, "
					+ CITY + " TEXT, "
					+ STATE + " TEXT, "
					+ COUNTRY + " TEXT, "
					+ STATIONID + " TEXT, "
					+ WEATHER + " TEXT, "
					+ TEMPF + " TEXT, "
					+ TEMPC + " TEXT, "
					+ HUMIDITY + " TEXT, "
					+ WINDSTRING + " TEXT, "
					+ WINDDIR + " TEXT, "
					+ WINDMPH + " TEXT, "
					+ WINDKPH + " TEXT, "
					+ FEELSLIKEF + " TEXT, "
					+ FEELSLIKEC + " TEXT, "
					+ VISIBILITYMI + " TEXT, "
					+ VISIBILITYKM + " TEXT, "
					+ POPDAY + " TEXT, "
					+ POPNIGHT + " TEXT, "
					+ PRECIPIN + " TEXT, "
					+ PRECIPMM + " TEXT, "
					+ DESCDAY + " TEXT, "
                    + DESCNIGHT + " TEXT, "
                    + SUNRISE + " TEXT, "
					+ SUNSET + " TEXT);";
		
		db.execSQL(sql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	public void addRecord(String date, String lat, String lon, String city, String state, String country, String stationID,
			String weather, String tempF, String tempC, String humidity, String windString, String windDir, String windMPH,
			String windKPH, String feelsLikeF, String feelsLikeC, String visibilityMI, String visibilityKM, String popDay,
			String popNight, String precipIN, String precipMM, String descDay, String descNight, String sunrise, String sunset)
	{
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DATE, date);
		values.put(LAT, lat);
		values.put(LON, lon);
		values.put(CITY, city);
		values.put(STATE, state);
		values.put(COUNTRY, country);
		values.put(STATIONID, stationID);
		values.put(WEATHER, weather);
		values.put(TEMPF, tempF);
		values.put(TEMPC, tempC);
		values.put(HUMIDITY, humidity);
		values.put(WINDSTRING, windString);
		values.put(WINDDIR, windDir);
		values.put(WINDMPH, windMPH);
		values.put(WINDKPH, windKPH);
		values.put(FEELSLIKEF, feelsLikeF);
		values.put(FEELSLIKEC, feelsLikeC);
		values.put(VISIBILITYMI, visibilityMI);
		values.put(VISIBILITYKM, visibilityKM);
		values.put(POPDAY, popDay);
		values.put(POPNIGHT, popNight);
		values.put(PRECIPIN, precipIN);
		values.put(PRECIPMM, precipMM);
		values.put(DESCDAY, descDay);
		values.put(DESCNIGHT, descNight);
        values.put(SUNRISE, sunrise);
        values.put(SUNSET, sunset);
		
		db.insert(TABLE_NAME, null, values);
	}
	
	public Cursor allRows()
	{
		/* 
		 * Returns a cursor to all rows and all columns in the database in the order in which they were initialized
		 */
		
		String[] from = { _ID, DATE, LAT, LON, CITY, STATE, COUNTRY, STATIONID, WEATHER, TEMPF, TEMPC, HUMIDITY, WINDSTRING, WINDDIR, WINDMPH, WINDKPH, FEELSLIKEF, FEELSLIKEC, VISIBILITYMI, VISIBILITYKM, POPDAY, POPNIGHT, PRECIPIN, PRECIPMM, DESCDAY, DESCNIGHT, SUNRISE, SUNSET};
		
		String order = _ID;
		
		SQLiteDatabase db = getWritableDatabase();
		return db.query(TABLE_NAME, from, null, null, null, null, order);
	}
	
	public Cursor searchRows(String key, String value)
	{
		/*
		 * Returns a Cursor to all columns in the db where rows are matched by the incoming key-value pair
		 */
		String[] from = { _ID, DATE, LAT, LON, CITY, STATE, COUNTRY, STATIONID, WEATHER, TEMPF, TEMPC, HUMIDITY, WINDSTRING, WINDDIR, WINDMPH, WINDKPH, FEELSLIKEF, FEELSLIKEC, VISIBILITYMI, VISIBILITYKM, POPDAY, POPNIGHT, PRECIPIN, PRECIPMM, DESCDAY, DESCNIGHT};
		String order = _ID;
		String where = key + " = ?";
		String[] args = new String[] {value};
		
		SQLiteDatabase db = getWritableDatabase();
		return db.query(TABLE_NAME, from, where, args, null, null, order);
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
	
	public void deleteAll()
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
