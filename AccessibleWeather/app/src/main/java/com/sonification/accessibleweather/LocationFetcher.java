package com.sonification.accessibleweather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.sonification.accessibleweather.databases.DatabaseCachedWeather;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Jehoshaph Akshay on 03-12-2015.
 * This file fetches the location for us using various methods
 */

public class LocationFetcher extends Service
{
    String latitude;
    String longitude;

    static final int STATE_READY = 0;
    static final int STATE_TRYING = 1;
    static final int STATE_SUCCESS = 2;
    static final int STATE_FAILED = 3;

    public static final int CONFIDENT = 1;
    public static final int UNCERTAIN = 0;

    public static final String CERTAINTY_KEY = "certainty";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";

    public static final String BROADCAST_KEY = "location_fix_broadcast";

    boolean forceConfidentLocation = false;
    public static final String FORCE_CONFIDENT_LOCATION_KEY = "force_confident_location";

    int method1State;
    int method2State;

    boolean wundergroundIPSuccess = false;

    LocationManager locationManager;

    static final int FINE_LOCATION_TIMEOUT = 60000; // 1 minute timeout

    Handler handler;

    @Override
    public IBinder onBind(Intent workIntent)
    {
        return null;
        // Do nothing here for now, a later implementation will update the thread here from settings
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null) {
            return Service.START_STICKY;
        }
        Bundle bundle = intent.getExtras();
        // Force confident location if there are no extras or if the extra asks to force location
        forceConfidentLocation = (bundle == null) || bundle.getBoolean(FORCE_CONFIDENT_LOCATION_KEY);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        // start fetching location
        fetchLocation();
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    private void fetchLocation() {
        // Get the last known location
        boolean methodLastKnownSuccess = methodLastKnown();

        if (!methodLastKnownSuccess) {
            // Try to get using wunderground IP
            LoadData task = new LoadData();
            task.execute();
        }
        else {
            sendMessage(UNCERTAIN);
        }
    }

    private void getFineLocationFix() {
        method1State = STATE_READY;
        method2State = STATE_READY;
        String providerName = LocationManager.GPS_PROVIDER;
        if (locationManager.isProviderEnabled(providerName)) {
            locationManager.requestSingleUpdate(providerName, locationListener, null);
            // Start a runnable that ends method 1 in FINE_LOCATION_TIMEOUT
            handler = new Handler();
            final Runnable method1Runnable = new Runnable() {
                public void run() {
                    if (method1State == STATE_TRYING) {
                        method1State = STATE_FAILED;
                        locationManager.removeUpdates(locationListener);
                        getFineLocationFix();
                    }
                }
            };
            handler.postDelayed(method1Runnable, FINE_LOCATION_TIMEOUT);
            method1State = STATE_TRYING;
        } else {
            method1State = STATE_FAILED;
        }

        if (method2State == STATE_READY && method1State == STATE_FAILED) {
            providerName = LocationManager.NETWORK_PROVIDER;
            if (locationManager.isProviderEnabled(providerName)) {
                locationManager.requestSingleUpdate(providerName, locationListener, null);
                // Start a runnable that ends method 2 in FINE_LOCATION_TIMEOUT
                handler = new Handler();
                final Runnable method2Runnable = new Runnable() {
                    public void run() {
                        if (method2State == STATE_TRYING) {
                            method2State = STATE_FAILED;
                            locationManager.removeUpdates(locationListener);
                            getFineLocationFix();
                        }
                    }
                };
                handler.postDelayed(method2Runnable, FINE_LOCATION_TIMEOUT);
                method2State = STATE_TRYING;
            } else {
                method2State = STATE_FAILED;
            }
        }

        if (method1State == STATE_FAILED && method2State == STATE_FAILED) {
            latitude = "0";
            longitude = "0";
            sendMessage(CONFIDENT);
        }

        /*else
        {
            Log.e("Final statement", "It got to it");
            // No usable location - Prompt the user to enter a location manually at first
            if(prefs.getValue(getString(R.string.SETUP_COMPLETED_KEY), false)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityStartup.this);

                alertDialogBuilder
                        .setTitle(getString(R.string.manualLocationString))
                        .setMessage(getString(R.string.manualLocationDescriptionString))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fetchManualLocation();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                exitActivity();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                exitActivity();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
            else
            {
                // Use the last known latitude and longitude if we have setup before
                prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
                prefs.editValue(getString(R.string.LATITUDE_KEY), prefs.getValue(getString(R.string.LAST_LAT), "0"));
                prefs.editValue(getString(R.string.LONGITUDE_KEY), prefs.getValue(getString(R.string.LAST_LON), "0"));
                LoadData task = new LoadData();
                task.execute();
            }
            /*prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
            prefs.editValue(getString(R.string.LATITUDE_KEY), prefs.getDefaultLatitude());
            prefs.editValue(getString(R.string.LONGITUDE_KEY), prefs.getDefaultLongitude());
            LoadData task = new LoadData();
            task.execute();*/

        //}
        /*
        if(attemptNumber < 3)
        {
            Log.e("Attempting location", "" + attemptNumber);
            Log.e("Provider Last", providerName);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if (attemptNumber < 2) {
                        try {
                            Log.e("Thread", "Entered: " + attemptNumber);
                            int elapsedTime = 0;
                            while (elapsedTime < TIME_OUT && !locationSuccess) {
                                elapsedTime = elapsedTime + 1000;
                                Log.e("Thread", "Time: " + elapsedTime);
                                sleep(1000);
                                handler.post(this);
                            }
                            Log.e("Thread", "Exiting");
                            locationManager.removeUpdates(locationListener);
                            attemptNumber++;
                        } catch (InterruptedException e) {
                            // Interrupted here
                            attemptNumber++;
                        }
                    }

                }
            };
            thread.start();
            attemptNumber++;

            //locationManager.requestLocationUpdates(providerName, 0, 0, locationListener);
        }*/
    }

    private boolean methodDatabase() {
        DatabaseCachedWeather dbCache = new DatabaseCachedWeather(LocationFetcher.this);
        Cursor cacheCursor = dbCache.allRows();
        if(cacheCursor.moveToFirst())
        {
            latitude = Double.toString(cacheCursor.getDouble(2));
            longitude = Double.toString(cacheCursor.getDouble(3));
            cacheCursor.close();
            dbCache.close();
            return true;
        }
        cacheCursor.close();
        dbCache.close();
        return false;
    }

    private boolean methodWundergroundIP() {
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
            return false;
        }
        catch (IOException e)
        {
            return false;
        }

        try {
            JSONObject mainObject = new JSONObject(responseString);
            JSONObject observationArray = mainObject.getJSONObject("current_observation");
            JSONObject displayLocation = observationArray.getJSONObject("display_location");

            latitude = displayLocation.getString("latitude");
            longitude = displayLocation.getString("longitude");

        } catch(JSONException e) {
            return false;
        }

        return true;
    }

    private boolean methodLastKnown() {
        LocationManager lm = (LocationManager)LocationFetcher.this.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            return false;
        }
        else {
            longitude = Double.toString(location.getLongitude());
            latitude = Double.toString(location.getLatitude());
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

    private void sendMessage(int certainty) {
        /*
        Create a local app-wide broadcast with the lat lon and the certainty of the fix
         */

        Intent intent = new Intent(BROADCAST_KEY);
        // You can also include some extra data.
        intent.putExtra(CERTAINTY_KEY, certainty);
        intent.putExtra(LATITUDE_KEY, latitude);
        intent.putExtra(LONGITUDE_KEY, longitude);
        LocalBroadcastManager.getInstance(LocationFetcher.this).sendBroadcast(intent);
    }

    LocationListener locationListener = new LocationListener()
    {
        // Listens for a location update that we call using any of the given methods

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            // Do nothing
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            // Do nothing
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            // Do nothing
        }

        @Override
        public void onLocationChanged(Location location)
        {
            if(location != null)
            {
                latitude = Double.toString(location.getLatitude());
                longitude = Double.toString(location.getLongitude());
                if (method1State == STATE_TRYING) {
                    method1State = STATE_SUCCESS;
                }
                if (method2State == STATE_TRYING) {
                    method2State = STATE_SUCCESS;
                }
                sendMessage(CONFIDENT);
            }
            else
            {
                if (method1State == STATE_TRYING) {
                    method1State = STATE_FAILED;
                }
                if (method2State == STATE_TRYING) {
                    method2State = STATE_FAILED;
                }
                getFineLocationFix();
            }
        }
    };

    // Hit wunderground IP in the background
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
            // Get the Lat lon from IP
            wundergroundIPSuccess = methodWundergroundIP();

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            if (wundergroundIPSuccess) {
                sendMessage(UNCERTAIN);
            }
            else if (methodDatabase()) {
                sendMessage(UNCERTAIN);
            }
            else {
                latitude = "0";
                longitude = "0";
                sendMessage(UNCERTAIN);
            }
            if (forceConfidentLocation) {
                // Now take our sweet time getting a good location fix
                getFineLocationFix();
            }
        }
    }
}
