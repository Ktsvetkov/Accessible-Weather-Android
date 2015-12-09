package com.sonification.accessibleweather.definitions;

import java.util.Calendar;

public class GlobalVariables
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Class stores global variables
	 * Uses some getter-setters to change these variables
	 */

    // Use number
    public static final int MAX_TRACK_USE_NUMBER = 200;
    public static final int REVIEW_USE_NUMBER = 10;

    // Bundle keys
    public static final String WEATHER_CONDITIONS_KEY = "weather_conditions";

	// Time definitions
	public static final int NIGHT_START = 19;
	public static final int DAY_START = 6;
    public static final String NEVER_UPDATED = "199110231030";

    // My intra application private keys
    public static final String LATITUDE_KEY = "lat_key";
    public static final String LONGITUDE_KEY = "lon_key";

    // Global variables for astronomy json object
    public static final String ASTRONOMY_SUN_PHASE_KEY = "sun_phase";
    public static final String ASTRONOMY_SUNRISE_KEY = "sunrise";
    public static final String ASTRONOMY_SUNSET_KEY = "sunset";
    public static final String ASTRONOMY_HOUR_KEY = "hour";
    public static final String ASTRONOMY_MINUTE_KEY = "minute";
	
	// global variables for conditions json object
	public static final String CONDITIONS_OBSERVATION_KEY = "current_observation";
	public static final String CONDITIONS_LOCATION_KEY = "display_location";
	public static final String STATE_NAME_KEY = "state_name";
	public static final String CITY_NAME_KEY = "city";
	public static final String COUNTRY_NAME_KEY = "state_name";
	public static final String WEATHER_KEY = "weather";
	public static final String TEMP_F_KEY = "temp_f";
	public static final String TEMP_C_KEY = "temp_c";
	public static final String RELATIVE_HUMIDITY_KEY = "relative_humidity";
	public static final String WIND_STRING_KEY = "wind_string";
	public static final String STATION_ID_KEY = "station_id";
	public static final String WIND_DIR_KEY = "wind_dir";
	public static final String WIND_MPH_KEY = "wind_mph";
	public static final String WIND_KPH_KEY = "wind_kph";
	public static final String FEELS_LIKE_F_KEY = "feelslike_f";
	public static final String FEELS_LIKE_C_KEY = "feelslike_c";
	public static final String VISIBILITY_MI_KEY = "visibility_mi";
	public static final String VISIBILITY_KM_KEY = "visibility_km";
	public static final String PRECIP_TODAY_IN_KEY = "precip_today_in";
	public static final String PRECIP_TODAY_METRIC_KEY = "precip_today_metric";
    public static final String LOCAL_TIMEZONE_KEY = "local_tz_offset";
	
	// global variables for 10daysforecast jsonobject
	public static final String FORECAST_DAILY_KEY = "forecast";
	public static final String TEXT_FORECAST_DAILY_KEY = "txt_forecast";
	public static final String FORECAST_DAILY_ARRAY_KEY = "forecastday";
	public static final String FORECAST_DAILY_TITLE_KEY = "title";
	public static final String FORECAST_DAILY_POP_KEY = "pop";
	public static final String SIMPLE_FORECAST_DAILY_KEY = "simpleforecast";
	public static final String FORECAST_DAILY_DATE_KEY = "date";
	public static final String FORECAST_DAILY_DAY_KEY = "day";
	public static final String FORECAST_DAILY_MONTH_KEY = "month";
	public static final String FORECAST_DAILY_YEAR_KEY = "year";
	public static final String FORECAST_DAILY_HIGH_KEY = "high";
	public static final String FORECAST_DAILY_LOW_KEY = "low";
	public static final String FORECAST_DAILY_FAHRENHEIT_KEY = "fahrenheit";
	public static final String FORECAST_DAILY_CELCIUS_KEY = "celsius";
	public static final String FORECAST_DAILY_CONDITIONS_KEY = "conditions";
	public static final String FORECAST_DAILY_SNOW_DAY_KEY = "snow_day";
	public static final String FORECAST_DAILY_SNOW_NIGHT_KEY = "snow_night";
	public static final String FORECAST_DAILY_INCHES_KEY = "in";
	public static final String FORECAST_DAILY_CMS_KEY = "cm";

    // global variables for hourly jsonobject
    public static final String HOURLY_FORECAST_KEY = "hourly_forecast"; //Returns a JSON array
    public static final String TIME_KEY = "FCTTIME";                    //Returns a JSON object
    public static final String HOUR_KEY = "hour";                       //Returns a String
    public static final String YEAR_KEY = "year";                       //Returns a String
    public static final String MONTH_KEY = "mon_padded";                //Returns a String
    public static final String DAY_KEY = "mday_padded";                 //Returns a String
    public static final String TEMP_KEY = "temp";                       //Returns a JSON object
    public static final String ENGLISH_KEY = "english";                 //Returns a String
    public static final String METRIC_KEY = "metric";                   //Returns a String
    public static final String CONDITION_KEY = "condition";             //Returns a String
    public static final String WIND_SPEED_KEY = "wspd";                 //Returns a JSON object
    public static final String FEELS_LIKE_TEMP_KEY = "feelslike";       //Returns a JSON object
    public static final String POP_KEY = "pop";                         //Returns a String

    // Global variables for JSON geo lookup keys
    public static final String GEONAMES_ARRAY_KEY = "geonames";         //Returns a JSON array
    public static final String GEO_COUNTRY_NAME_KEY = "countryName";    //Returns a String
    public static final String GEO_CITY_NAME_KEY = "name";              //Returns a String
    public static final String GEO_LAT_KEY = "lat";                     //Returns a String
    public static final String GEO_LON_KEY = "lng";                     //Returns a String
    public static final String GEO_RESULTS_KEY = "totalResultsCount";   //Returns a String

    // Link stuff
    public static final String CHARACTER_SET = "utf-8";
	public static final String WUNDERGROUND_URL = "http://api.wunderground.com/";
	public static final String WUNDERGROUND_API_KEY_URL = "/16337742f9b11efe";
	public static final String CONDITIONS_API_URL = "/conditions/q";
	public static final String FORECAST_10_DAYS_API_URL = "/forecast10day/q";
	public static final String HOURLY_API_URL = "/hourly/q";
    public static final String ASTRONOMY_API_URL = "/astronomy/q";
	public static final String WUNDERGROUND_API_URL = "http://api.wunderground.com/api";
	public static final String JSON_API_URL = ".json";
    public static final String WUNDERGROUND_REFERRAL_KEY = "http://www.wunderground.com/?apiref=55beedd6f3da1057";

    // Notification IDS
    public static final int WEATHER_UPDATE_NOTIFICATION_ID = 1;

    // Geonames URLs
    public static final String GEONAMES_SEARCH1_URL = "http://api.geonames.org/searchJSON?q=";
    public static final String GEONAMES_SEARCH2_URL = "&maxRows=1&username=sonlab@gatech.edu";

    //Time definitions
	public static final int MAX_STALENESS = 21600000; // Maximum time within which no updates are acceptable (6 hours - in millis)
	public static final int MIN_STALENESS = 1800000;	  // Minimum time within which an update is not required (30 minutes - in millis)
    public static final int NOTIFICATION_UPDATE_INTERVAL = 1800000; // Update the notification every 30 minutes
    public static final int SERVICE_START_DELAY = 60000;    // Delay start of service thread by 1 minute

    private static boolean serviceRunning = false;

    // Tab view maintenance
	private static int currentTab = 0;
	private static boolean[] viewCreated = {false, false, false, false};
	public static final int TOTAL_TABS = 4;
	
	public void setCurrentTab(int curTab)
	{
		currentTab = curTab;
	}
	
	public int getCurrentTab()
	{
		return currentTab;
	}
	
	public void setViewCreated(int position, boolean value)
	{
		viewCreated[position] = value;
	}
	
	public boolean getViewCreated(int position)
	{
		return viewCreated[position];
	}

    public static boolean isDay(Calendar cal)
    {
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        return hour < NIGHT_START && hour >= DAY_START;
    }

    public static boolean isDay(int hours)
    {
        return hours < NIGHT_START && hours >= DAY_START;
    }

    // Service maintenance
    public boolean isServiceRunning()
    {
        return serviceRunning;
    }

    public void setServiceRunning(boolean serviceState)
    {
        serviceRunning = serviceState;
    }
}
