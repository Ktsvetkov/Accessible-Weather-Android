package com.sonification.accessibleweather.intent_services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.sonification.accessibleweather.ActivityStartup;
import com.sonification.accessibleweather.R;
import com.sonification.accessibleweather.WeatherFetcher;
import com.sonification.accessibleweather.databases.DatabaseCachedWeather;
import com.sonification.accessibleweather.databases.DatabaseHourlyWeather;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.ResourceHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class IntentServiceNotifications extends Service
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
     * This describes an intent service that runs in the background to provide the user with weather updates
     * It runs at wake time and usual travel time to provide notifications to the user with the current weather
     * Additionally, it can be configured to run before a certain inclement weather condition is encountered
     */

    PreferencesHelper prefs;
    NotificationCompat.Builder mBuilder;

    boolean loadSuccess = false;
    int mNotificationID = GlobalVariables.WEATHER_UPDATE_NOTIFICATION_ID;

    // Units
    String temperatureUnits;
    String visibilityUnits;
    String windUnits;

    // Header metrics
    String conditions;

    // Body metrics
    String currentTemperature;
    String highTemp;
    String lowTemp;
    String snowRain;
    String sunrise;
    String sunset;
    String currentWind;
    String currentVisibility;

    // Preference values
    boolean conditionsCheck;
    boolean currentTempCheck;
    boolean highLowCheck;
    boolean snowRainCheck;
    boolean sunriseCheck;
    boolean sunsetCheck;
    boolean currentWindCheck;
    boolean currentVisibilityCheck;

    LocationManager locationManager;

    final Handler handler = new Handler();
    Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            handler.postDelayed(this, GlobalVariables.NOTIFICATION_UPDATE_INTERVAL);
            LoadData task = new LoadData();
            task.execute();
        }
    };

    private boolean updateWeatherData()
    {
        // If in manual mode, fetch location from prefs
        if(prefs.getValue(getString(R.string.MANUAL_LOCATION_KEY), false))
        {
            String lat = prefs.getValue(getString(R.string.LAST_LAT), "0");
            String lon = prefs.getValue(getString(R.string.LAST_LON), "0");
            prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
            prefs.editValue(getString(R.string.LATITUDE_KEY), lat);
            prefs.editValue(getString(R.string.LONGITUDE_KEY), lon);
        }

        WeatherFetcher wFetch = new WeatherFetcher(IntentServiceNotifications.this);
        String responseCode = wFetch.fetchAndUpdateData(WeatherFetcher.LOAD_MODE_LAZY);

        if(responseCode.equals(WeatherFetcher.LOCATION_ERROR))
        {
            // Use the last used location
            prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
            prefs.editValue(getString(R.string.LATITUDE_KEY), prefs.getValue(getString(R.string.LAST_LAT), "0"));
            prefs.editValue(getString(R.string.LONGITUDE_KEY), prefs.getValue(getString(R.string.LAST_LON), "0"));
            LoadData task = new LoadData();
            task.execute();
        }
        // We are not going to fetch a fresh location (use the old notification) - leave that up to the Android OS
        return (responseCode.equals(WeatherFetcher.LOAD_SUCCESS) || responseCode.equals(WeatherFetcher.CONNECTION_ERROR_USABLE_DATABASE) || responseCode.equals(WeatherFetcher.RECENT_DATABASE_UPDATE));
    }

    private void updateNotification()
    {
        Calendar cal = Calendar.getInstance();

        parseData();    // Update our values from the database
        mBuilder.setContentText(getCondensedWeatherText());
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), ResourceHelper.getLargeIcon(conditions, cal.get(Calendar.HOUR_OF_DAY))));
        mBuilder.setSmallIcon(ResourceHelper.getSmallIcon(conditions, cal.get(Calendar.HOUR_OF_DAY)));
        mBuilder.setTicker(getTitleText());
        mBuilder.setContentTitle(getTitleText());
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(getWeatherText()));

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationID, mBuilder.build());
    }

    private void parseData()
    {
        Calendar cal = Calendar.getInstance();

        String dbTemp = "";
        String dbPopDay = "";
        String dbPopNight = "";
        String dbSunrise = "";
        String dbSunset = "";
        String dbWindSpeed = "";
        String dbWindDir = "";
        String dbVisibility = "";

        boolean snowRainLater = false;
        boolean isMetric = prefs.isMetric();

        DatabaseCachedWeather dbCache = new DatabaseCachedWeather(IntentServiceNotifications.this);
        Cursor cacheCursor = dbCache.allRows();

        if(cacheCursor.moveToFirst())
        {
            // Get stuff we need from the cache database
            conditions = cacheCursor.getString(8);
            dbPopDay = cacheCursor.getString(20);
            dbPopNight = cacheCursor.getString(21);
            if(isMetric)
            {
                dbTemp = cacheCursor.getString(17);
                dbWindSpeed = cacheCursor.getString(15);
                dbVisibility = cacheCursor.getString(19);
            }
            else
            {
                dbTemp = cacheCursor.getString(16);
                dbWindSpeed = cacheCursor.getString(14);
                dbVisibility = cacheCursor.getString(18);
            }
            dbSunrise = cacheCursor.getString(26);
            dbSunset = cacheCursor.getString(27);
            dbWindDir = cacheCursor.getString(13);
        }

        if(dbTemp == null || dbTemp.equalsIgnoreCase("null") || dbTemp.equals(""))
        {
            dbTemp = "0";
        }
        double roundTemp = Double.parseDouble(dbTemp);
        dbTemp = Integer.toString((int)roundTemp);

        temperatureUnits = StringDefinitions.UNICODE_DEGREE;
        windUnits = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SPEED, isMetric);
        visibilityUnits = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_DISTANCE, isMetric);

        cacheCursor.close();
        dbCache.close();

        // Current temperature
        currentTemperature = "Feels like: " + dbTemp + temperatureUnits;

        // Current wind
        double rounderWind;
        try
        {
            rounderWind = Double.parseDouble(dbWindSpeed);
        }
        catch(Exception e)
        {
            rounderWind = 0.0;
        }
        dbWindSpeed = Integer.toString((int)rounderWind);
        currentWind = "Wind: ";
        if(dbWindDir.length() > 0)
        {
            currentWind = currentWind + StringDefinitions.GET_WIND_DESC(dbWindDir) + " ";
        }
        currentWind = currentWind + "at " + dbWindSpeed + " ";
        currentWind = currentWind + windUnits;

        // Set visibility
        currentVisibility = "Visibility: " + dbVisibility + visibilityUnits;

        // Done with all the cache database things, load the hourly database if required
        if(highLowCheck || snowRainCheck)
        {
            String[] hours = new String[24];
            int[] feelsLikeTemps = new int[24];
            String[] conditions = new String[24];

            // Load the forecast from the database
            DatabaseHourlyWeather dbHourly = new DatabaseHourlyWeather(IntentServiceNotifications.this);
            Cursor hourlyCursor = dbHourly.allRows();

            int maxTemp = -273; // Absolute zero
            int minTemp = 5778; // The temperature of the surface of the sun
            int maxIndex = 0;
            int minIndex = 0;
            int extremeConditionIndex = 0;

            if (hourlyCursor.moveToFirst())
            {
                for (int i = 0; i < 24; i++)
                {
                    hours[i] = hourlyCursor.getString(1);
                    conditions[i] = hourlyCursor.getString(7);

                    if (isMetric) {
                        feelsLikeTemps[i] = (int) Float.parseFloat(hourlyCursor.getString(11));    // Possibility of breaking here
                    } else {
                        feelsLikeTemps[i] = (int) Float.parseFloat(hourlyCursor.getString(10));    // Possibility of breaking here
                    }

                    // Find the max and min temps and their indices
                    if (maxTemp < feelsLikeTemps[i])
                    {
                        maxTemp = feelsLikeTemps[i];
                        maxIndex = i;
                    }
                    if (minTemp > feelsLikeTemps[i])
                    {
                        minTemp = feelsLikeTemps[i];
                        minIndex = i;
                    }

                    // Find the first instance of rain, snow, thunder etc. if not already found
                    if(!snowRainLater)
                    {
                        int actualCondition = StringDefinitions.GET_CONDITION(conditions[i]);
                        if(actualCondition == StringDefinitions.CONDITION_SNOW_KEY || actualCondition == StringDefinitions.CONDITION_RAIN_KEY ||
                           actualCondition == StringDefinitions.CONDITION_FLURRIES_KEY || actualCondition == StringDefinitions.CONDITION_SLEET_KEY ||
                           actualCondition == StringDefinitions.CONDITION_STORM_KEY)
                        {
                            // One of the extreme conditions
                            extremeConditionIndex = i;
                            snowRainLater = true;
                        }
                    }
                    hourlyCursor.moveToNext();
                }
            }
            hourlyCursor.close();
            dbHourly.close();

            // Highs
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh aa", Locale.ENGLISH);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[maxIndex]));
            highTemp = Integer.toString(maxTemp);
            highTemp = "High: " + highTemp + temperatureUnits + " at " + dateFormat.format(cal.getTime());

            // Lows
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[minIndex]));
            lowTemp = Integer.toString(minTemp);
            lowTemp = "Low: " + lowTemp + temperatureUnits + " at " + dateFormat.format(cal.getTime());

            // Set extreme conditions text
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[extremeConditionIndex]));
            snowRain = conditions[extremeConditionIndex];
            snowRain = snowRain + " at " + dateFormat.format(cal.getTime());
        }

        // Get sunrise and sunset times
        sunrise = "Sunrise: " + get12HourString(dbSunrise);
        sunset = "Sunset: " + get12HourString(dbSunset);

        // Reset the cal to the current time
        cal.setTimeInMillis(System.currentTimeMillis());
        // If no chance of snow or rain later today, default to the Wunderground predicted values
        if(cal.get(Calendar.HOUR_OF_DAY) >= GlobalVariables.DAY_START && cal.get(Calendar.HOUR_OF_DAY) < GlobalVariables.NIGHT_START)
        {
            if(!snowRainLater)
            {
                snowRain = dbPopDay;
            }
        }
        else
        {
            if(!snowRainLater)
            {
                snowRain = dbPopNight;
            }
        }
        if(!snowRainLater)
        {
            if (snowRain == null || snowRain.equalsIgnoreCase("null") || snowRain.length() < 1)
            {
                snowRain = "0";
            }
            snowRain = "Chance of rain: " + snowRain + "%";
        }
    }

    private String getTitleText()
    {
        String titleText = "Weather";
        if(conditionsCheck)
        {
            titleText = titleText + ": " + conditions;
        }

        return titleText;
    }

    private String getCondensedWeatherText()
    {
        String bodyText = "";
        int lines = 0;
        if(currentTempCheck)
        {
            bodyText = bodyText + currentTemperature;
            lines++;
        }
        if(highLowCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + highTemp + ", " + lowTemp;
            lines++;
        }
        if(snowRainCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + snowRain;
            lines++;
        }
        if(sunriseCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + sunrise;
            lines++;
        }
        if(sunsetCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + sunset;
            lines++;
        }
        if(currentWindCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + currentWind;
            lines++;
        }
        if(currentVisibilityCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + ", ";
            }
            bodyText = bodyText + currentVisibility;
        }

        return bodyText;
    }

    private String getWeatherText()
    {
        String bodyText = "";
        int lines = 0;
        if(currentTempCheck)
        {
            bodyText = bodyText + currentTemperature;
            lines++;
        }
        if(highLowCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + highTemp + "\n" + lowTemp;
            lines++;
        }
        if(snowRainCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + snowRain;
            lines++;
        }
        if(sunriseCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + sunrise;
            lines++;
        }
        if(sunsetCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + sunset;
            lines++;
        }
        if(currentWindCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + currentWind;
            lines++;
        }
        if(currentVisibilityCheck)
        {
            if(lines > 0)
            {
                bodyText = bodyText + "\n";
            }
            bodyText = bodyText + currentVisibility;
        }

        return bodyText;
    }

    @Override
    public IBinder onBind(Intent workIntent)
    {
        return null;
        // Do nothing here for now, a later implementation will update the thread here from settings
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        prefs = new PreferencesHelper(IntentServiceNotifications.this);

        if(!prefs.getValue(getString(R.string.SETUP_COMPLETED_KEY), false))
        {
            // Exit if we haven't been setup
            this.stopSelf();
        }

        handler.postDelayed(runnable, GlobalVariables.SERVICE_START_DELAY);

        GlobalVariables globalVars = new GlobalVariables();
        globalVars.setServiceRunning(true);

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy()
    {
        GlobalVariables globalVars = new GlobalVariables();
        globalVars.setServiceRunning(false);
        super.onDestroy();
    }

    private void buildBasicNotification()
    {
        mBuilder = new NotificationCompat.Builder(this);

        // Create a pendingIntent that starts our application
        Intent startApplication = new Intent(this, ActivityStartup.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, startApplication, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Do not make it persistent
        mBuilder.setOngoing(false);
        mBuilder.setAutoCancel(false);

        // Do not alert every time it updates, don't display time
        mBuilder.setWhen(0);
        mBuilder.setOnlyAlertOnce(true);
    }

    private void destroyNotification()
    {
        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(mNotificationID);
    }

    public class LoadData extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            // Do nothing
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            asyncTaskPerform();

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            /*
            Now, display the notification using our updated databases
             */
            prefs = new PreferencesHelper(IntentServiceNotifications.this);

            if(loadSuccess)
            {
                if(prefs.getValue(IntentServiceNotifications.this.getString(R.string.DISPLAY_NOTIFICATION), true))
                {
                    /*
                    Update preference values
                    Build the notification
                    Display the notification
                     */
                    updateFromPrefs();
                    buildBasicNotification();
                    updateNotification();
                }
                else
                {
                    /*
                    Cancel the notification
                     */
                    destroyNotification();
                }
            }
        }
    }

    private void updateFromPrefs()
    {
        /*
        Updates the local variables from the stored prefs.
         */
        conditionsCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_CONDITIONS_KEY), true);
        currentTempCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_CURRENT_TEMP_KEY), true);
        highLowCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_VARIATION_DAY_KEY), true);
        snowRainCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_SNOW_RAIN_DAY_KEY), true);
        sunriseCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_SUNRISE_KEY), false);
        sunsetCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_SUNSET_KEY), false);
        currentWindCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_WIND_KEY), false);
        currentVisibilityCheck = prefs.getValue(getResources().getString(R.string.NOTIFICATION_CURRENT_VISIBILITY_KEY), false);

        windUnits = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SPEED, prefs.isMetric());
        visibilityUnits = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_DISTANCE, prefs.isMetric());
    }

    private String get12HourString(String time24Hours)
    {
        if(time24Hours.length() != 4)
        {
            return ("Unavailable");
        }

        String hours = time24Hours.substring(0,2);
        String minutes = time24Hours.substring(2,4);

        Calendar time = Calendar.getInstance();

        time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours));
        time.set(Calendar.MINUTE, Integer.parseInt(minutes));
        String minute = String.format("%02d", time.get(Calendar.MINUTE));

        // time.get(Calendar.AM_PM) returns integer 0 or 1 so let's set the right String value
        String AM_PM = time.get(Calendar.AM_PM) == 0 ? "AM" : "PM";

        return (time.get(Calendar.HOUR) + ":" + minute + " " + AM_PM);
    }

    private void asyncTaskPerform()
    {
        loadSuccess = updateWeatherData();
    }
}