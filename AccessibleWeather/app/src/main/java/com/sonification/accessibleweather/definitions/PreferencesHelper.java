package com.sonification.accessibleweather.definitions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.sonification.accessibleweather.R;

public class PreferencesHelper
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Class that stores methods to access and edit stored preferences
	 * Other activities and services should call this method when they need to use preferences
	 */
	
	SharedPreferences sharedPrefs;
	
	/*
	 * Overriding the editValue, getValue functions to accept and return any kind of value
	 */

    Context context;
	
	public PreferencesHelper(Context context)
	{
		/*
		 * The default android implementation of shared preferences needs the instance to be associated
		 * with the calling activity or service. This is passed from the calling in context
		 */
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
	}
	
	public void editValue(String key, String value)
	{
		Editor editor = sharedPrefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

    public void editValue(String key, int value)
    {
        Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }
	
	public void editValue(String key, boolean value)
	{
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public void setDefaultLocation(String lat, String lon) {
		Editor editor = sharedPrefs.edit();
		editor.putString("latitude", lat);
		editor.putString("longitude", lon);
		editor.apply();
	}
	
	public String getValue(String key, String defaultValue)
	{
		if(sharedPrefs.contains(key))
		{
			return sharedPrefs.getString(key, defaultValue);
		}
		else
		{
			return defaultValue;
		}
	}
	
	public boolean getValue(String key, boolean defaultValue)
	{
		if(sharedPrefs.contains(key))
		{
			return sharedPrefs.getBoolean(key, defaultValue);
		}
		else
		{
			return defaultValue;
		}
	}

    public int getValue(String key, int defaultValue)
    {
        if(sharedPrefs.contains(key))
        {
            return sharedPrefs.getInt(key, defaultValue);
        }
        else
        {
            return defaultValue;
        }
    }

	public String getDefaultLatitude() {
		if(sharedPrefs.contains("latitude"))
		{
			return sharedPrefs.getString("latitude","0");
		}
		else
		{
			return "0";
		}
	}


	public String getDefaultLongitude() {
		if(sharedPrefs.contains("longitude"))
		{
			return sharedPrefs.getString("longitude", "0");
		}
		else
		{
			return "0";
		}
	}

    public boolean isMetric()
    {
        String units = sharedPrefs.getString(context.getString(R.string.UNIT_KEY), context.getString(R.string.DEFAULT_UNIT));

        return units.equalsIgnoreCase("metric");
    }
}
