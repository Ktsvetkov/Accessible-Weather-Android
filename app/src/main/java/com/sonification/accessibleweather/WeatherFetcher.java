package com.sonification.accessibleweather;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sonification.accessibleweather.databases.DatabaseCachedWeather;
import com.sonification.accessibleweather.databases.DatabaseDailyWeather;
import com.sonification.accessibleweather.databases.DatabaseHourlyWeather;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class WeatherFetcher
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Utility class to fetch weather data from the wunderground server and store in the databases
	 * This class will try to be as graceful as possible,
	 * That is it should return appropriate error messages and make use of try catch blocks to avoid errors.
	 * Due to the nature of the work that this class is performing, most use errors may originate here
	 * 
	 * Provide a method to check if connectivity is present and the wunderground server is up and running
	 * Provide a method to update the cached (day to day) weather
	 * Provide a method to update the hourly weather
	 * Provide a method to update the daily weather
	 */
	public static final String CONNECTION_ERROR_STALE_DATABASE = "Couldn't connect to the internet";	// Failure case
	public static final String LOCATION_ERROR = "Updating location...";	// Failure case
	public static final String CONNECTION_ERROR_USABLE_DATABASE = "Connection error, usable database";  // Success
	public static final String PARSING_ERROR = "Error fetching data from wunderground"; // Failure case, remove later
	public static final String RECENT_DATABASE_UPDATE = "Database updated recently, no need to fetch data"; // Success
	public static final String LOAD_SUCCESS = "Data loaded successfully";   // Success
	
	/*
	 * I recommend you use LOAD_MODE_LAZY for most of your update calls
	 * LOAD_MODE_LAZY will not reload data if it has been loaded recently (saves on API calls).
	 * LOAD_MODE_FORCE on the other hand forces a fresh fetch of the data.
	 * This is useful in some cases such as when the user has just changed some preferences,
	 * you would like to reload data forcefully
	 */
	public static final int LOAD_MODE_LAZY = 1;
	
	Context context;
	PreferencesHelper prefs;
	
	String lastUpdateDate;
	
	int year;
	int month;
	int day;
	int hour;
	int minute;
	long timeDef;
	
	Calendar cal;
	Calendar currentCal;
	
	double latitude = 0;
	double longitude = 0;
	
	String date;
	String lat;
	String lon;
	String city;
	String state;
	String country;
	String stationID;
	String weather;
	String tempF;
	String tempC;
	String humidity;
	String windString;
	String windDir;
	String windMPH;
	String windKPH;
	String feelsLikeF;
	String feelsLikeC;
	String visibilityMI;
	String visibilityKM;
	String popDay;			//loaded from forecast
	String popNight;		//loaded from forecast
	String precipIN;
	String precipMM;
	String descDay;			//loaded from forecast
	String descNight;		//loaded from forecast
    String sunrise;         //loaded from astronomy
    String sunset;          //loaded from astronomy
	
	boolean metric;
	
	public WeatherFetcher(Context context)
	{
		this.context = context;
		prefs = new PreferencesHelper(context);

		metric = prefs.isMetric();
		
		cal = Calendar.getInstance();
		currentCal = Calendar.getInstance();
	}
	
	public String fetchAndUpdateData(int mode)
	{
		/*
		 * Master method, this fetches all data by calling methods one by one
		 * When any part of this module fails, the appropriate error message is returned
		 * Otherwise the success message is displayed.
		 * 
		 */
		
		if(recentDatabaseUpdate() && mode == LOAD_MODE_LAZY)
		{
			// Recent database, no need to update anything
			return RECENT_DATABASE_UPDATE;
		}
		else
		{
			// Database update preferable
			if(connectionExists())
			{
				// Connection to the wunderground server exists
				if(getLatLon())
				{
					// Good! connections to the servers exist and we have a reasonable latlon
					boolean dbUpdate = updateDatabasesFromServer();

                    // Save the lat lon for the future
                    prefs.editValue(context.getString(R.string.LAST_LAT), lat);
                    prefs.editValue(context.getString(R.string.LAST_LON), lon);
					
					if(dbUpdate)
					{
						// Everything went well, return a success code
						prefs.editValue(context.getString(R.string.LAST_UPDATE_DATE_KEY), calendarToString(currentCal));
						return LOAD_SUCCESS;
					}
				}
				else
				{
                    if(oldDatabaseUpdate())
                    {
                        // We can't get a location update - falling back to old database
                        return CONNECTION_ERROR_USABLE_DATABASE;
                    }
					// We can't use an old database resort to other methods for getting location
					return LOCATION_ERROR;
				}
			}
			
			// Connection doesn't exist/unable to load data, switching to worst case scenario
			if(oldDatabaseUpdate())
			{
				// Connection doesn't exist but databases exist that have been updated
				// within a reasonable window
				return CONNECTION_ERROR_USABLE_DATABASE;
			}
			else
			{
				// Usable database doesn't exist and there is a connection error
				// Nothing the application can do (This is one of three failure cases)
				return CONNECTION_ERROR_STALE_DATABASE;
			}
		}
	}
	
	private boolean connectionExists()
	{
		/*
		 * Checks if the weather underground URL is reachable from the application
		 * The URL may be unreachable for a number of reasons not only if the server is down.
		 * (For example: the URL is banned in the country or blocked by the network admin)
		 */
		if (isNetworkAvailable(context))
		{
			try
			{
				HttpURLConnection urlC = (HttpURLConnection) (new URL(GlobalVariables.WUNDERGROUND_URL).openConnection());
	            urlC.setRequestProperty("User-Agent", "Test");
	            urlC.setRequestProperty("Connection", "close");
	            urlC.setConnectTimeout(3000);
	            urlC.connect();
	            return (urlC.getResponseCode() == HttpURLConnection.HTTP_OK);
	        }
			catch(IOException e)
			{
                //Do nothing
	        }
	    }

	    return false;
	}
	
	private boolean isNetworkAvailable(Context context)
	{
		/*
		 * Checks if the device has an internet connection
		 * or is connected to a wifi network
		 */
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	
	private boolean oldDatabaseUpdate()
	{
		/*
		 * Sad case :(, method checks if there is enough data in the databases to start the application
		 * even if there is no connectivity, uses MAX_STALENESS
		 */
		
		lastUpdateDate = prefs.getValue(context.getString(R.string.LAST_UPDATE_DATE_KEY), "199110231030");
		
		cal = stringToCal(lastUpdateDate);
		timeDef = currentCal.getTimeInMillis() - cal.getTimeInMillis();
		return (timeDef <= GlobalVariables.MAX_STALENESS);
	}
	
	private boolean recentDatabaseUpdate()
	{
		/*
		 * Happy case :), method checks if there is enough data in the databases to start the application
		 * without the need for updating, uses MIN_STALENESS
		 */
		
		lastUpdateDate = prefs.getValue(context.getString(R.string.LAST_UPDATE_DATE_KEY), GlobalVariables.NEVER_UPDATED);

		cal = stringToCal(lastUpdateDate);
		timeDef = currentCal.getTimeInMillis() - cal.getTimeInMillis();
		return (timeDef <= GlobalVariables.MIN_STALENESS);
	}
	
	private boolean getLatLon()
	{
		/*
		 * Gets latest latitude, longitude
		 * return false if unavailable:
		 *
		 * Check if this is a load_mode_force method meaning I have to use the provided lat, lon
		 * Check if there is a last known location available
		 * Check if location is available in cache that is not too old
		 * As a last resort get the coarse location (or) get the fine GPS location
		 */
        boolean load_mode_force = prefs.getValue(context.getString(R.string.LOAD_MODE_FORCE_KEY), false);
        if(load_mode_force)
        {
            latitude = Double.parseDouble(prefs.getValue(context.getString(R.string.LATITUDE_KEY), "0.0"));
            longitude = Double.parseDouble(prefs.getValue(context.getString(R.string.LONGITUDE_KEY), "0.0"));
            prefs.editValue(context.getString(R.string.LOAD_MODE_FORCE_KEY), false);
            return true;
        }

		LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null)
		{
			if(oldDatabaseUpdate())
			{
				DatabaseCachedWeather dbCache = new DatabaseCachedWeather(context);
				Cursor cacheCursor = dbCache.allRows();
				if(cacheCursor.moveToFirst())
				{
					latitude = cacheCursor.getDouble(2);
					longitude = cacheCursor.getDouble(3);
					cacheCursor.close();
					dbCache.close();
					return true;
				}
				cacheCursor.close();
				dbCache.close();
			}

            // Note!! If a location is not available, the startup activity handles scheduling
            // for a fresh location update.

			//Get lat long from weather underground autoip

			return false;
		}
		else
		{
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			return true;
		}
	}

	private String convertStreamToString(InputStream is)
	{
		StringBuilder total = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		try
		{
            String line;
			while((line = rd.readLine()) != null)
			{
				total.append(line);
			}
		}
		catch(Exception e)
		{
            // Do nothing
		}
		
		return total.toString();
	}
	
	private boolean updateDatabasesFromServer()
	{
		/*
		 * Fetches data, if it is available and update databases
		 * I am using a httpget method (API sent as part of URI - generally not secure)
		 * If in future the API changes (for the better!) and they require the API key to be sent separately,
		 * this has to be changed to a httppost method
		 */
		
		if(updateCachedDatabase() && updateDailyDatabase() && updateHourlyDatabase() && updateAstronomyDatabase())
		{
			DatabaseCachedWeather dbCache = new DatabaseCachedWeather(context);
			if(dbCache.count() > 0)
			{
				dbCache.deleteAll();
			}
			dbCache.addRecord(date, lat, lon, city, state, country, stationID, weather, tempF, tempC, humidity, windString, windDir, windMPH, windKPH, feelsLikeF, feelsLikeC, visibilityMI, visibilityKM, popDay, popNight, precipIN, precipMM, descDay, descNight, sunrise, sunset);
			dbCache.close();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean updateCachedDatabase()
	{
		InputStream iS;
		String linkConditions = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
							+ GlobalVariables.CONDITIONS_API_URL + "/" + Double.toString(latitude) + "," + Double.toString(longitude) + GlobalVariables.JSON_API_URL;
		
		String responseString;
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(linkConditions);
		try
		{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			iS = entity.getContent();
			responseString = convertStreamToString(iS);
		}
		catch (ClientProtocolException e)
		{
			responseString = "E: " + e;
		}
		catch (IOException e)
		{
			responseString = "E: " + e;
		}
		
		return parseCacheString(responseString);
	}
	
	private boolean updateDailyDatabase()
	{
		InputStream iS;
		String linkConditions = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
							+ GlobalVariables.FORECAST_10_DAYS_API_URL + "/" + Double.toString(latitude) + "," + Double.toString(longitude) + GlobalVariables.JSON_API_URL;
		
		String responseString;
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(linkConditions);
		try
		{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			iS = entity.getContent();
			responseString = convertStreamToString(iS);
		}
		catch (ClientProtocolException e)
		{
			responseString = "E: " + e;
		}
		catch (IOException e)
		{
			responseString = "E: " + e;
		}

        return parseDailyString(responseString);
	}

	private boolean getLatLongAutoIP()
	{
		InputStream iS;

		String linkLatLong = "http://api.wunderground.com/api/16337742f9b11efe/conditions/q/autoip.json";

		String responseString;

		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(linkLatLong);
		try
		{
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			iS = entity.getContent();
			responseString = convertStreamToString(iS);
		}
		catch (ClientProtocolException e)
		{
			responseString = "E: " + e;
		}
		catch (IOException e)
		{
			responseString = "E: " + e;
		}

		return parseLatLongString(responseString);
	}

    private boolean updateHourlyDatabase()
    {
        InputStream iS;
        String linkHourly = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
                + GlobalVariables.HOURLY_API_URL + "/" + Double.toString(latitude) + "," + Double.toString(longitude) + GlobalVariables.JSON_API_URL;

        String responseString;

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(linkHourly);
        try
        {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            iS = entity.getContent();
            responseString = convertStreamToString(iS);
        }
        catch (ClientProtocolException e)
        {
            responseString = "E: " + e;
        }
        catch (IOException e)
        {
            responseString = "E: " + e;
        }

        return parseHourlyString(responseString);
    }

    private boolean updateAstronomyDatabase()
    {
        InputStream iS;
        String linkAstronomy = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
                + GlobalVariables.ASTRONOMY_API_URL + "/" + Double.toString(latitude) + "," + Double.toString(longitude) + GlobalVariables.JSON_API_URL;

        String responseString;

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(linkAstronomy);
        try
        {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            iS = entity.getContent();
            responseString = convertStreamToString(iS);
        }
        catch (ClientProtocolException e)
        {
            responseString = "E: " + e;
        }
        catch (IOException e)
        {
            responseString = "E: " + e;
        }

        return parseAstronomyString(responseString);
    }
	
	private boolean parseCacheString(String response)
	{
		/*
		 * This method doesn't immediately store the values in the cache database.
		 * Some of the values that have to be stored in the cache database have to be fetched from the forecast link.
		 * Wunderground really should update their API (This application will then break and have to be updated)
		 */
		try
		{
			JSONObject summaryObject = new JSONObject(response);
			
		    JSONObject entries = summaryObject.getJSONObject(GlobalVariables.CONDITIONS_OBSERVATION_KEY);
		    JSONObject location = entries.getJSONObject(GlobalVariables.CONDITIONS_LOCATION_KEY);
		    
		    date = calendarToString(currentCal);
			lat = Double.toString(latitude);
			lon = Double.toString(longitude);
			city = location.getString(GlobalVariables.CITY_NAME_KEY);
			state = location.getString(GlobalVariables.STATE_NAME_KEY);
			country = location.getString(GlobalVariables.COUNTRY_NAME_KEY);
			
			stationID = entries.getString(GlobalVariables.STATION_ID_KEY);
			weather = entries.getString(GlobalVariables.WEATHER_KEY);
			tempF = entries.getString(GlobalVariables.TEMP_F_KEY);
			tempC = entries.getString(GlobalVariables.TEMP_C_KEY);
			humidity = entries.getString(GlobalVariables.RELATIVE_HUMIDITY_KEY);
			windString = entries.getString(GlobalVariables.WIND_STRING_KEY);
			windDir = entries.getString(GlobalVariables.WIND_DIR_KEY);
			windMPH = entries.getString(GlobalVariables.WIND_MPH_KEY);
			windKPH = entries.getString(GlobalVariables.WIND_KPH_KEY);
			feelsLikeF = entries.getString(GlobalVariables.FEELS_LIKE_F_KEY);
			feelsLikeC = entries.getString(GlobalVariables.FEELS_LIKE_C_KEY);
			visibilityMI = entries.getString(GlobalVariables.VISIBILITY_MI_KEY);
			visibilityKM = entries.getString(GlobalVariables.VISIBILITY_KM_KEY);
			precipIN = entries.getString(GlobalVariables.PRECIP_TODAY_IN_KEY);
			precipMM = entries.getString(GlobalVariables.PRECIP_TODAY_METRIC_KEY);
		}
		catch(JSONException e)
		{
			return false;
		}
		
		return true;
	}
	
	private boolean parseDailyString(String response)
	{
		/*
		 * Parses the string response obtained by the forecast10day call
		 * and updates the corresponding values.
		 * This method directly saves to the database to conserve on this methods resource footprint
		 */
		DatabaseDailyWeather dbDailyWeather = new DatabaseDailyWeather(context);
		
		int arrLength;
		String[] dTitle;
		String[] dPop;
		String[] dDay;
		String[] dMonth;
		String[] dYear;
		String[] dHigh_F;
        String[] dHigh_C;
        String[] dLow_F;
        String[] dLow_C;
        String[] dConditions;
		String[] dSnow_F;
        String[] dSnow_C;

        try
		{
			JSONObject summaryObject = new JSONObject(response);
			
		    JSONObject entries = summaryObject.getJSONObject(GlobalVariables.FORECAST_DAILY_KEY);
		    
		    JSONObject textForecast = entries.getJSONObject(GlobalVariables.TEXT_FORECAST_DAILY_KEY);
		    JSONArray forecastDayArray = textForecast.getJSONArray(GlobalVariables.FORECAST_DAILY_ARRAY_KEY);
		    
		    arrLength = forecastDayArray.length();
		    
		    dTitle = new String[arrLength];
		    dPop = new String[arrLength];
		    dDay = new String[arrLength];
		    dMonth = new String[arrLength];
		    dYear = new String[arrLength];
		    dHigh_F = new String[arrLength];
            dHigh_C = new String[arrLength];
            dLow_F = new String[arrLength];
            dLow_C = new String[arrLength];
		    dConditions = new String[arrLength];
		    dSnow_F = new String[arrLength];
            dSnow_C = new String[arrLength];

            for(int i = 0; i < arrLength; i++)
		    {
		    	JSONObject period = forecastDayArray.getJSONObject(i);
		    	dTitle[i] = period.getString(GlobalVariables.FORECAST_DAILY_TITLE_KEY);
		    	dPop[i] = period.getString(GlobalVariables.FORECAST_DAILY_POP_KEY);
		    	
		    	if(i == 0)
		    	{
		    		popDay = dPop[i];
		    		descDay = dTitle[i];
		    	}
		    	else if(i == 1)
		    	{
		    		popNight = dPop[i];
		    		descNight = dTitle[i];
		    	}
		    }
		    
		    JSONObject simpleForecast = entries.getJSONObject(GlobalVariables.SIMPLE_FORECAST_DAILY_KEY);
		    forecastDayArray = simpleForecast.getJSONArray(GlobalVariables.FORECAST_DAILY_ARRAY_KEY);
		    
		    int arrLength2 = forecastDayArray.length();	// Unfortunately arrLength here is arrLength / 2 of previous arrLength, see docs for JSON explanation
		    
		    for(int i = 0; i < arrLength2; i++)
		    {
		    	JSONObject period = forecastDayArray.getJSONObject(i);
		    	
		    	JSONObject date = period.getJSONObject(GlobalVariables.FORECAST_DAILY_DATE_KEY);
		    	dDay[i * 2] = date.getString(GlobalVariables.FORECAST_DAILY_DAY_KEY);
		    	dDay[(i * 2) + 1] = dDay[i * 2];
		    	// Note: If you noticed that an integer is being returned here. Good job!! and thanks for reading the docs.
		    	// This call coerces the integer into a string (avoids breaking in future if Wunderground switches to a string)
		    	dMonth[i * 2] = date.getString(GlobalVariables.FORECAST_DAILY_MONTH_KEY);
		    	dMonth[(i * 2) + 1] = dMonth[i * 2];
		    	dYear[i * 2] = date.getString(GlobalVariables.FORECAST_DAILY_YEAR_KEY);
		    	dYear[(i * 2) + 1] = dYear[i * 2];
		    	
		    	JSONObject high = period.getJSONObject(GlobalVariables.FORECAST_DAILY_HIGH_KEY);

                dHigh_F[i * 2] = high.getString(GlobalVariables.FORECAST_DAILY_FAHRENHEIT_KEY);
                dHigh_F[(i * 2) + 1] = dHigh_F[i * 2];
                dHigh_C[i * 2] = high.getString(GlobalVariables.FORECAST_DAILY_CELCIUS_KEY);
                dHigh_C[(i * 2) + 1] = dHigh_C[i * 2];//com/sonification/accessibleweather/WeatherFetcher.java:663


		    	JSONObject low = period.getJSONObject(GlobalVariables.FORECAST_DAILY_LOW_KEY);

                dLow_F[i * 2] = low.getString(GlobalVariables.FORECAST_DAILY_FAHRENHEIT_KEY);
                dLow_F[(i * 2) + 1] = dLow_F[i * 2];
                dLow_C[i * 2] = low.getString(GlobalVariables.FORECAST_DAILY_CELCIUS_KEY);
                dLow_C[(i * 2) + 1] = dLow_C[i * 2];

		    	dConditions[i * 2] = period.getString(GlobalVariables.FORECAST_DAILY_CONDITIONS_KEY);
		    	dConditions[(i * 2) + 1] = dConditions[i * 2];
		    	
		    	JSONObject snowDay = period.getJSONObject(GlobalVariables.FORECAST_DAILY_SNOW_DAY_KEY);

                dSnow_F[i * 2] = snowDay.getString(GlobalVariables.FORECAST_DAILY_INCHES_KEY);
                if(dSnow_F[i * 2] == null || dSnow_F[i * 2].equalsIgnoreCase("null"))
                {
                    dSnow_F[i * 2] = "0.0";
                }

                dSnow_C[i * 2] = snowDay.getString(GlobalVariables.FORECAST_DAILY_CMS_KEY);
                if(dSnow_C[i * 2] == null || dSnow_C[i * 2].equalsIgnoreCase("null"))
                {
                    dSnow_C[i * 2] = "0.0";
                }

		    	JSONObject snowNight = period.getJSONObject(GlobalVariables.FORECAST_DAILY_SNOW_NIGHT_KEY);

                dSnow_F[(i * 2) + 1] = snowNight.getString(GlobalVariables.FORECAST_DAILY_INCHES_KEY);
                if(dSnow_F[i * 2] == null || dSnow_F[i * 2].equalsIgnoreCase("null"))
                {
                    dSnow_F[i * 2] = "0.0";
                }

                dSnow_C[(i * 2) + 1] = snowNight.getString(GlobalVariables.FORECAST_DAILY_CMS_KEY);
                if(dSnow_C[i * 2] == null || dSnow_C[i * 2].equalsIgnoreCase("null"))
                {
                    dSnow_C[i * 2] = "0.0";
                }

		    }
		}
		catch(JSONException e)
		{
			return false;
		}
		
		if(dbDailyWeather.count() > 0)
		{
			dbDailyWeather.deleteAllEntries();
		}
		
		for(int j = 0; j < arrLength; j++)
		{
			dbDailyWeather.addRecord(dDay[j], dMonth[j], dYear[j], dTitle[j], dPop[j], dSnow_F[j], dSnow_C[j], dHigh_F[j], dHigh_C[j], dLow_F[j], dLow_C[j], dConditions[j]);
		}
		
		dbDailyWeather.close();
		
		return true;
	}

	public boolean parseLatLongString(String response) {
		try {

			JSONObject mainObject = new JSONObject(response);

			JSONArray observationArray = mainObject.getJSONArray("current_observation");

			JSONObject displayLocation = observationArray.getJSONObject(1);

			String lat = displayLocation.getString("latitude");
			String lon = displayLocation.getString("longitude");

			Log.e("AutoIPLat", lat + "");
			Log.e("AutoIPLon", lon + "");


			prefs.setDefaultLocation(lat, lon);

		} catch(JSONException e) {
				return false;
		}
		return true;
	}

    public boolean parseHourlyString(String response)
    {
        DatabaseHourlyWeather dbHourlyWeather = new DatabaseHourlyWeather(context);

        int arrLength;
        String[] dHour;
        String[] dDay;
        String[] dMonth;
        String[] dYear;
        String[] dTemp_F;
		String[] dTemp_C;
		String[] dConditions;
        String[] dWindSpeed_F;
		String[] dWindSpeed_C;
		String[] dFeelsLikeTemp_F;
		String[] dFeelsLikeTemp_C;
		String[] dPop;


		try
        {
            JSONObject summaryObject = new JSONObject(response);

            JSONArray hourlyArray = summaryObject.getJSONArray(GlobalVariables.HOURLY_FORECAST_KEY);

            arrLength = hourlyArray.length();

            dHour = new String[arrLength];
            dDay = new String[arrLength];
            dMonth = new String[arrLength];
            dYear = new String[arrLength];
            dTemp_F = new String[arrLength];
			dTemp_C = new String[arrLength];
			dConditions = new String[arrLength];
            dWindSpeed_F = new String[arrLength];
			dWindSpeed_C = new String[arrLength];
			dFeelsLikeTemp_F = new String[arrLength];
			dFeelsLikeTemp_C = new String[arrLength];
			dPop = new String[arrLength];

            for(int i = 0; i < arrLength; i++)
            {
                JSONObject hourlyObjects = hourlyArray.getJSONObject(i);

                JSONObject timeObject = hourlyObjects.getJSONObject(GlobalVariables.TIME_KEY);
                dHour[i] = timeObject.getString(GlobalVariables.HOUR_KEY);
                dDay[i] = timeObject.getString(GlobalVariables.DAY_KEY);
                dMonth[i] = timeObject.getString(GlobalVariables.MONTH_KEY);
                dYear[i] = timeObject.getString(GlobalVariables.YEAR_KEY);

                dConditions[i] = hourlyObjects.getString(GlobalVariables.CONDITION_KEY);
                dPop[i] = hourlyObjects.getString(GlobalVariables.POP_KEY);

				dTemp_F[i] = hourlyObjects.getJSONObject(GlobalVariables.TEMP_KEY).getString(GlobalVariables.ENGLISH_KEY);
				dTemp_C[i] = hourlyObjects.getJSONObject(GlobalVariables.TEMP_KEY).getString(GlobalVariables.METRIC_KEY);
				dWindSpeed_F[i] = hourlyObjects.getJSONObject(GlobalVariables.WIND_SPEED_KEY).getString(GlobalVariables.ENGLISH_KEY);
				dWindSpeed_C[i] = hourlyObjects.getJSONObject(GlobalVariables.WIND_SPEED_KEY).getString(GlobalVariables.METRIC_KEY);
				dFeelsLikeTemp_F[i] = hourlyObjects.getJSONObject(GlobalVariables.FEELS_LIKE_TEMP_KEY).getString(GlobalVariables.ENGLISH_KEY);
				dFeelsLikeTemp_C[i] = hourlyObjects.getJSONObject(GlobalVariables.FEELS_LIKE_TEMP_KEY).getString(GlobalVariables.METRIC_KEY);
            }
        }
        catch(JSONException e)
        {
            return false;
        }

        if(dbHourlyWeather.count() > 0)
        {
            dbHourlyWeather.deleteAllEntries();
        }

        for(int j = 0; j < arrLength; j++)
        {
            dbHourlyWeather.addRecord(dHour[j], dDay[j], dMonth[j], dYear[j], dTemp_F[j], dTemp_C[j], dConditions[j], dWindSpeed_F[j], dWindSpeed_C[j], dFeelsLikeTemp_F[j], dFeelsLikeTemp_C[j], dPop[j]);
        }

        dbHourlyWeather.close();

        return true;
    }
	
	private String calendarToString(Calendar cal)
	{
		/*
		 * Converts a calendar object to a string representation of the object
		 * Formatted as YYYYMMDDhhmm
		 */
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		
		String date = Integer.toString(year);
		String monthS = Integer.toString(month);
		String dayS = Integer.toString(day);
		String hourS = Integer.toString(hour);
		String minuteS = Integer.toString(minute);
		
		if(monthS.length() < 2)
		{
			monthS = "0" + monthS;
		}
		if(dayS.length() < 2)
		{
			dayS = "0" + dayS;
		}
		if(hourS.length() < 2)
		{
			hourS = "0" + hourS;
		}
		if(minuteS.length() < 2)
		{
			minuteS = "0" + minuteS;
		}
		
		date = date + monthS + dayS + hourS + minuteS;
		
		return date;
	}
	
	private Calendar stringToCal(String date)
	{
		/*
		 * Returns a Calendar object for the passed date
		 * Note that string should be in YYYYMMDDhhmm
		 */
		Calendar tempCal = Calendar.getInstance();
		
		year = Integer.parseInt(date.substring(0, 4));
		month = Integer.parseInt(date.substring(4, 6));
		day = Integer.parseInt(date.substring(6, 8));
		hour = Integer.parseInt(date.substring(8, 10));
		minute = Integer.parseInt(date.substring(10, 12));
		
		tempCal.set(Calendar.YEAR, year);
		tempCal.set(Calendar.MONTH, month - 1);
		tempCal.set(Calendar.DAY_OF_MONTH, day);
		tempCal.set(Calendar.HOUR_OF_DAY, hour);
		tempCal.set(Calendar.MINUTE, minute);
		
		return tempCal;
	}

    private boolean parseAstronomyString(String response)
    {
		/*
		 * This method doesn't immediately store the values in the cache database.
		 * Some of the values that have to be stored in the cache database have to be fetched from the forecast link.
		 */
        try
        {
            JSONObject summaryObject = new JSONObject(response);

            JSONObject sunPhaseObject = summaryObject.getJSONObject(GlobalVariables.ASTRONOMY_SUN_PHASE_KEY);
            JSONObject sunriseObject = sunPhaseObject.getJSONObject(GlobalVariables.ASTRONOMY_SUNRISE_KEY);
            JSONObject sunsetObject = sunPhaseObject.getJSONObject(GlobalVariables.ASTRONOMY_SUNSET_KEY);

            String tempHour = sunriseObject.getString(GlobalVariables.ASTRONOMY_HOUR_KEY);
            String tempMinute = sunriseObject.getString(GlobalVariables.ASTRONOMY_MINUTE_KEY);
            if(tempHour.length() == 1)
            {
                tempHour = "0" + tempHour;
            }
            if(tempMinute.length() == 1)
            {
                tempMinute = "0" + tempMinute;
            }
            sunrise = tempHour + tempMinute;

            tempHour = sunsetObject.getString(GlobalVariables.ASTRONOMY_HOUR_KEY);
            tempMinute = sunsetObject.getString(GlobalVariables.ASTRONOMY_MINUTE_KEY);
            if(tempHour.length() == 1)
            {
                tempHour = "0" + tempHour;
            }
            if(tempMinute.length() == 1)
            {
                tempMinute = "0" + tempMinute;
            }
            sunset = tempHour + tempMinute;
        }
        catch(JSONException e)
        {
            return false;
        }

        return true;
    }
}