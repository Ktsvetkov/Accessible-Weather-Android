package com.sonification.accessibleweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
    TextView loadingText;
    GlobalVariables globalVars;
	
	String responseCode = "";

    int useNumber;
    boolean locationSuccess = false;

    PreferencesHelper prefs;

    boolean mainStarted = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

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
        loadingText = (TextView)findViewById(R.id.loadingText);

        // Register for location updates
        Intent locationFetcherIntent = new Intent(this, LocationFetcher.class);
        locationFetcherIntent.putExtra(LocationFetcher.FORCE_CONFIDENT_LOCATION_KEY, false);
        this.startService(locationFetcherIntent);
        // Register to receive location update broadcasts
        LocalBroadcastManager.getInstance(ActivityStartup.this).registerReceiver(mMessageReceiver, new IntentFilter(LocationFetcher.BROADCAST_KEY));
        loadingLayout.setVisibility(LinearLayout.VISIBLE);
        loadingText.setText(getResources().getText(R.string.loadingLocationString));
	}

    // Our handler called whenever LocationFetcher broadcasts
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int certainty = intent.getIntExtra(LocationFetcher.CERTAINTY_KEY, LocationFetcher.UNCERTAIN);
            String latitude = intent.getStringExtra(LocationFetcher.LATITUDE_KEY);
            String longitude = intent.getStringExtra(LocationFetcher.LONGITUDE_KEY);

            if (latitude.equals("0") && longitude.equals("0")) {
                // We may have failed to fetch a location
                if (certainty == LocationFetcher.CONFIDENT) {
                    // All attempts to fetch location failed
                    fetchManualLocation();
                }
                else {
                    loadingLayout.setVisibility(LinearLayout.GONE);
                    startupErrorText.setVisibility(TextView.VISIBLE);
                    startupErrorText.setText(getResources().getText(R.string.manualLocationString));
                    startupErrorText.requestFocus();
                    startupErrorText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                }
            }
            else {
                if (!mainStarted) {
                    // Start the main activity if it is not running already
                    LoadData task = new LoadData(latitude, longitude);
                    task.execute();
                    mainStarted = true;
                }
            }
        }
    };
	
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

        String latitude;
        String longitude;

        public LoadData(String latitude, String longitude) {
            super();
            this.latitude = latitude;
            this.longitude = longitude;
        }
	    
		@Override
	    protected void onPreExecute()
	    {
	    	loadingLayout.setVisibility(LinearLayout.VISIBLE);
            loadingText.setText(getResources().getText(R.string.loadingString));
	    }
	    
	    @Override
	    protected Void doInBackground(Void... params)
	    {
	    	asyncTaskPerform(latitude, longitude);
	    	
	    	return null;
	    }
	    
	    @Override
	    protected void onPostExecute(Void result)
	    {
	        super.onPostExecute(result);
            loadingLayout.setVisibility(LinearLayout.GONE);
	        startupErrorText.setVisibility(TextView.VISIBLE);
	        if(responseCode.equals(WeatherFetcher.CONNECTION_ERROR_STALE_DATABASE) || responseCode.equals(WeatherFetcher.PARSING_ERROR))
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
	
	public void asyncTaskPerform(String latitude, String longitude)
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

		WeatherFetcher fetcher = new WeatherFetcher(ActivityStartup.this, latitude, longitude);
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
                String latitude = data.getStringExtra(ActivityManualLocation.MANUAL_LAT_KEY);
                String longitude = data.getStringExtra(ActivityManualLocation.MANUAL_LON_KEY);
                LoadData task = new LoadData(latitude, longitude);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}