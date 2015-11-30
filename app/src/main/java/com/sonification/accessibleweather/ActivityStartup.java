package com.sonification.accessibleweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sonification.accessibleweather.databases.DatabaseCachedWeather;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.intent_services.IntentServiceNotifications;

public class ActivityStartup extends Activity
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * This Activity called on application startup.
	 * This is responsible for displaying the start screen
	 * and starting the update services if they are not currently running.
	 * In addition, this activity is responsible for initiating the tutorial on first startup.
	 */
	LinearLayout loadingLayout;
	
	ProgressBar loadingBar;
	
	TextView splashScreenTitleText;
	TextView startupErrorText;
    GlobalVariables globalVars;
	
	String responseCode = "";

    Handler handler = new Handler();

    LocationManager locationManager;

    int attemptNumber = 0; //From good to bad, attempts at fetching location, 0 - Network, 1 - GPS, 2 - WiFi
    int useNumber;
    boolean locationSuccess = false;

    static final int TIME_OUT = 30000;

    PreferencesHelper prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        prefs = new PreferencesHelper(ActivityStartup.this);
        globalVars = new GlobalVariables();

        useNumber = prefs.getValue(getString(R.string.NUMBER_OF_USES_KEY), 0);

        if(useNumber == 0)
        {
            // Set some default values for our preferences
            prefs.editValue(getString(R.string.UNIT_KEY), getString(R.string.DEFAULT_UNIT));
            prefs.editValue(getString(R.string.SETUP_COMPLETED_KEY), false);
            prefs.editValue(getString(R.string.DISPLAY_NOTIFICATION), true);
            prefs.editValue(getString(R.string.HIGH_VISIBILITY_MODE), false);
            prefs.editValue(getString(R.string.PLAY_SONIFICATIONS), true);
            prefs.editValue(getString(R.string.SETUP_COMPLETED_KEY), false);
            prefs.editValue(getString(R.string.MANUAL_LOCATION_KEY), false);
            prefs.editValue(getString(R.string.LAST_LAT), "17.3700");
            prefs.editValue(getString(R.string.LAST_LON), "78.4800");
        }

        if(useNumber < GlobalVariables.MAX_TRACK_USE_NUMBER)
        {
            // Update the useNumber as long as it is less than the max tracker after which we don't care
            useNumber++;
        }
        prefs.editValue(getString(R.string.NUMBER_OF_USES_KEY), useNumber);
		loadSplashScreen();
	}
	
	public void loadSplashScreen()
	{
		setContentView(R.layout.layout_splashscreen);
		
		loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
		loadingBar = (ProgressBar)findViewById(R.id.loadingBar);
		splashScreenTitleText = (TextView)findViewById(R.id.splashScreenTitleText);
		startupErrorText = (TextView)findViewById(R.id.startupErrorText);

        LoadData task = new LoadData();
        task.execute();
	}
	
	public class LoadData extends AsyncTask<Void, Void, Void>
	{
		/*
		 * Start an async task that will run in the background
		 * Display the progressBar on pre-execute
		 * perform the async task
		 * Hide the progressBar and leave the activity once our task has been performed
		 * If there is an error loading databases or fetching data
		 * display the error messages here and exit after a couple of seconds (not implemented)
		 */
	    
		@Override
	    protected void onPreExecute()
	    {
	    	loadingLayout.setVisibility(LinearLayout.VISIBLE);
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
            //if(responseCode.equals(WeatherFetcher.LOCATION_ERROR))
            if(true)
            {
                // We do not have a cached location, make a fresh attempt
                getLocationUpdate();
            }
	        loadingLayout.setVisibility(LinearLayout.GONE);
	        startupErrorText.setVisibility(TextView.VISIBLE);
	        if(responseCode.equals(WeatherFetcher.CONNECTION_ERROR_STALE_DATABASE) || responseCode.equals(WeatherFetcher.PARSING_ERROR) || responseCode.equals(WeatherFetcher.LOCATION_ERROR))
	        {
	        	/*
	        	 * If, for some reason we are unable to proceed,
	        	 * display the error text, give it focus and read it out
	        	 */
	        	startupErrorText.setText(responseCode);
	        	startupErrorText.requestFocus();
				startupErrorText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
	        }
	        else
	        {
                locationSuccess = true;
	        	startMainActivity();
	        	finish();
	        }
	    }
	}
	
	public void asyncTaskPerform()
	{
		/*
		 * Start our notification updater service if it isn't running
		 * Starts a service that fetches weather data using HTTPGET
		 * The service then stores the data in the appropriate databases
		 * I am using a service to ensure robustness
		 */
        if(!globalVars.isServiceRunning())
        {
            Intent notificationService = new Intent(ActivityStartup.this, IntentServiceNotifications.class);
            startService(notificationService);
        }

        // If in manual mode, fetch location from prefs
        if(prefs.getValue(getString(R.string.MANUAL_LOCATION_KEY), false))
        {
            String lat = prefs.getValue(getString(R.string.LAST_LAT), "0");
            String lon = prefs.getValue(getString(R.string.LAST_LON), "0");
            prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
            prefs.editValue(getString(R.string.LATITUDE_KEY), lat);
            prefs.editValue(getString(R.string.LONGITUDE_KEY), lon);
        }

		WeatherFetcher fetcher = new WeatherFetcher(ActivityStartup.this);
		responseCode = fetcher.fetchAndUpdateData(WeatherFetcher.LOAD_MODE_LAZY);
	}
	
	public void startMainActivity()
	{
		// Start main activity here, passing the weather conditions along as a bundled value

        String weatherConditions = "";

        DatabaseCachedWeather dbCache = new DatabaseCachedWeather(ActivityStartup.this);
        Cursor cacheCursor = dbCache.allRows();

        if(cacheCursor.moveToFirst())
        {
            weatherConditions = cacheCursor.getString(8);
        }

        cacheCursor.close();
        dbCache.close();

        //Create the bundle
        Bundle bundle = new Bundle();
        bundle.putString(GlobalVariables.WEATHER_CONDITIONS_KEY, weatherConditions);
		
		Intent startMainActivityIntent = new Intent(ActivityStartup.this, ActivityMain.class);
        startMainActivityIntent.putExtras(bundle);
		startActivity(startMainActivityIntent);
	}

    LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e("The location", location.getLatitude() + location.getLongitude() + "");
            if(location != null)
            {
                String lat = Double.toString(location.getLatitude());
                String lon = Double.toString(location.getLongitude());
                prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
                prefs.editValue(getString(R.string.LATITUDE_KEY), lat);
                prefs.editValue(getString(R.string.LONGITUDE_KEY), lon);
                LoadData task = new LoadData();
                task.execute();
            }
            else
            {
                getLocationUpdate();
            }
        }
    };

    private void getLocationUpdate()
    {
        String providerName = "";

        // Start keeping track of the time here
        // Start a thread that TIME_OUT time in the future will return
        // On returning do nothing if we have gone forward else bump up the attemptNumber and call getLocationUpdate() again
        // And also remove the location listener



        if (attemptNumber == 0) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                providerName = LocationManager.NETWORK_PROVIDER;
            } else {
                attemptNumber++;
            }
        }

        if (attemptNumber == 1) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                providerName = LocationManager.GPS_PROVIDER;
            } else {
                attemptNumber++;
            }
        }

        if (attemptNumber > 1) {
            //call method later
            Log.e("Location", "Done trying");
            attemptNumber++;
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
            locationManager.requestSingleUpdate(providerName, locationListener, null);
            //locationManager.requestLocationUpdates(providerName, 0, 0, locationListener);
        }

    }

    private void exitActivity()
    {
        finish();
    }

    private void fetchManualLocation()
    {
        /*
        Start the Activity Manual Location and waits for the result
        The current activity is in a paused state till then
         */
        Intent manualLocationActivity = new Intent(ActivityStartup.this, ActivityManualLocation.class);
        startActivityForResult(manualLocationActivity, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                // Get weather using a manual location
                prefs.editValue(getString(R.string.LOAD_MODE_FORCE_KEY), true);
                prefs.editValue(getString(R.string.LATITUDE_KEY), prefs.getValue(getString(R.string.LAST_LAT), "0"));
                prefs.editValue(getString(R.string.LONGITUDE_KEY), prefs.getValue(getString(R.string.LAST_LON), "0"));
                LoadData task = new LoadData();
                task.execute();
            }
            if(resultCode == RESULT_CANCELED)
            {
                // No usable location at all
                Toast.makeText(ActivityStartup.this, getString(R.string.noLocationString), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}