package com.sonification.accessibleweather;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sonification.accessibleweather.adapters.CustomDrawerAdapter;
import com.sonification.accessibleweather.adapters.TabsPagerAdapter;
import com.sonification.accessibleweather.databases.DatabasePreviousCities;
import com.sonification.accessibleweather.definitions.DrawerItem;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.ResourceHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;
import com.sonification.accessibleweather.parallax.ParallaxBackground;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityMain extends FragmentActivity
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * This class defines the behavior of the application on startup
	 * It mainly deals with maintenance and UI
	 * Data fetching is handled in separate processes
	 * Each Tab Fragment (com.sonification.accessibleweather.fragments) defines it own UI
	 * ActivityMain extends FragmentActivity
	 * Use - implements SwipeRefreshLayout.OnRefreshListener for a swipe layout
	 */

	GlobalVariables globalVars = new GlobalVariables();

    String weatherConditions = ""; // Initialize so that it's not null

    // SwipeRefreshLayout swipeLayout;
    MediaPlayer mediaPlayer;

    // Menu drawer
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    CustomDrawerAdapter drawerAdapter;

    private PreferencesHelper prefs;

	private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    private ParallaxBackground masterBackground;

    // Tab titles
    private String[] tabs = { StringDefinitions.TAB0, StringDefinitions.TAB1, StringDefinitions.TAB2, StringDefinitions.TAB3};
    private String[] tags = { ":0", ":1", ":2", ":3"};

    // Drawer entries
    List<DrawerItem> drawerItems = new ArrayList<>();
    List<String> lats = new ArrayList<>();
    List<String> lons = new ArrayList<>();
    int cityCount = 0;

    String itemCurrentName = "Current Location";
    String item1Name = "Lookup";
    String item2Name = "Settings";
    String item3Name = "Help & Feedback";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        // Retrieve the passed Weather conditions here
        Bundle bundle = getIntent().getExtras();
        weatherConditions = bundle.getString(GlobalVariables.WEATHER_CONDITIONS_KEY);
        prefs = new PreferencesHelper(ActivityMain.this);

        // Update the setup completed string
        if(!prefs.getValue(getString(R.string.SETUP_COMPLETED_KEY), false))
        {
            prefs.editValue(getString(R.string.SETUP_COMPLETED_KEY), true);
        }

		loadPage();
	}

	public void loadPage()
	{
        // Set the application theme here, before loading the page.
        //setTheme(ResourceHelper.getAppTheme(GlobalVariables.isDay(Calendar.getInstance()), weatherConditions));

        setContentView(R.layout.layout_main);

        // This makes sure our png images do not appear banded
        // getWindow().getAttributes().format = android.graphics.PixelFormat.RGBA_8888;

        /*
        Initializing the SwipeRefreshView
        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_purple,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        */
        // Initializing the parallax background
        masterBackground = (ParallaxBackground)findViewById(R.id.masterBackground);


		// Initializing the tab layout and navigation
        viewPager = (ViewPager)findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        for(int i = 0; i < 4; i++)
        {
            tags[i] = "android:switcher:" + R.id.pager + tags[i];
        }

        viewPager.setAdapter(mAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener()
        {
        	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
        	{
                // Show the given tab
        		viewPager.setCurrentItem(tab.getPosition());

                /*
                // Enable the swipeLayout on view 0 and 1 else disable it
                swipeLayout.setEnabled(tab.getPosition() == 0 || tab.getPosition() == 1);
                */

                /*if(tab.getPosition() == 0 && globalVars.getViewCreated(0))
                {
                    OverviewFragment fragOverview = (OverviewFragment) getSupportFragmentManager().findFragmentByTag(tags[0]);
                    fragOverview.doAccessibilityEventsDelayed();
                }*/

            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
            {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
            {
                // probably ignore this event
            }
        };

        // Adding Tabs
        for(String tab_name : tabs)
        {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(tabListener).setTag(tab_name));
        }

        // Tell our viewPager what to do when tab state changes
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
    	{
    	    @Override
    	    public void onPageSelected(int position)
    	    {
    	        actionBar.setSelectedNavigationItem(position);
        		globalVars.setCurrentTab(position);
    	        mAdapter.getItem(position).getActivity();
    	    }

            @Override
            public void onPageScrolled(int position, float percent, int pixOffset)
            {
                // this is called while user's flinging with:
                // position is the page number
                // percent is the percentage scrolled (0...1)
                // pixOffset is the pixel offset related to that percentage
                // so we got everything we need ....

                int totalPages = mAdapter.getCount(); // the total number of pages
                float finalPercentage = ((position + percent) * 100 / totalPages); // percentage of this page+offset respect the total pages
                setBackgroundX((int)finalPercentage);
            }

    	    @Override
    	    public void onPageScrollStateChanged(int arg0)
    	    {

    	    }
    	});

        // Initializing our menu drawer
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        ListView mainDrawerEntries = (ListView)findViewById(R.id.mainDrawerEntries);

        /* Our menu items
        Without manual locations    |   With manual locations
        1. Lookup                       1. Current Location
        2. Recent 1                     2. Lookup
        3. Recent 2                     3. Recent 1
        4. Recent 3                     4.
        5. Settings
        6. About
        7. Feedback
                                        8. Feedback
        */
        if(prefs.getValue(getString(R.string.MANUAL_LOCATION_KEY), false))
        {
            drawerItems.add(new DrawerItem(itemCurrentName, getResources().getIdentifier("map_current", "drawable", getPackageName())));
        }

        drawerItems.add(new DrawerItem(item1Name, getResources().getIdentifier("world_map_location" , "drawable", getPackageName())));
        DatabasePreviousCities dbPrevCities = new DatabasePreviousCities(ActivityMain.this);
        if(dbPrevCities.count() > 0)
        {
            int maxCount = 3;
            Cursor cityCursor = dbPrevCities.allRows();
            if(cityCursor.moveToLast())
            {
                while(!cityCursor.isBeforeFirst() && cityCount < maxCount)
                {
                    drawerItems.add(new DrawerItem(cityCursor.getString(1) + ", " + cityCursor.getString(2), getResources().getIdentifier("red_icon" , "drawable", getPackageName())));
                    lats.add(cityCursor.getString(3));
                    lons.add(cityCursor.getString(4));
                    cityCount++;
                    cityCursor.moveToPrevious();
                }
            }
            cityCursor.close();
        }
        dbPrevCities.close();

        drawerItems.add(new DrawerItem(item2Name, android.R.drawable.ic_menu_preferences));
        drawerItems.add(new DrawerItem(item3Name, android.R.drawable.ic_menu_help));

        //Initialize our adapter
        drawerAdapter = new CustomDrawerAdapter(this, R.layout.item_drawer_list, drawerItems);
        // Set the adapter for the list view
        mainDrawerEntries.setAdapter(drawerAdapter);
        // Set the onItemClickListener for this list
        mainDrawerEntries.setOnItemClickListener(new DrawerItemClickListener());

        // Initialize a drawer toggle listener
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawerOpenString, R.string.drawerCloseString)
        {
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                actionBar.setTitle(getString(R.string.display_app_name));
                // invalidateOptionsMenu(); // Use this if using an options menu
            }

            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(StringDefinitions.MENU_OPENED);
                // invalidateOptionsMenu();
            }
        };

        // Assign the listener to our drawer
        drawerLayout.setDrawerListener(mDrawerToggle);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Display dialog's as required based on use number
        int useNumber = prefs.getValue(getString(R.string.NUMBER_OF_USES_KEY), 1);

        if(useNumber == 1)
        {
            // Display the help dialog on first use
            displayOpenHelpDialog();
        }
        else if(useNumber == GlobalVariables.REVIEW_USE_NUMBER)
        {
            // Ask for a rating if the number of uses is appropriate
            displayRateAppDialog();
        }
	}

    private void displayOpenHelpDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityMain.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.helpString))
                .setMessage(getResources().getString(R.string.helpDescriptionString))
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.openHelpString), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Open the Help and Feedback section
                        helpAndFeedbackSelected();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.notNowString), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Inform about the navigation drawer
                        Toast.makeText(ActivityMain.this, getResources().getString(R.string.helpCancelledString), Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void displayRateAppDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityMain.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.feedbackString))
                .setMessage(getResources().getString(R.string.feedbackDescriptionString))
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.playStoreReviewString), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Go to PlayStore to review the application
                        playStoreReview();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.notNowString), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void playStoreReview()
    {
        // Opens the play store link for feedback
        Uri uri = Uri.parse("market://details?id=" + ActivityMain.this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try
        {
            startActivity(goToMarket);
        }
        catch(ActivityNotFoundException e)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + ActivityMain.this.getPackageName())));
        }
    }

    private void setBackgroundX(int scrollPosition)
    {
        // Sets the percent of the scroll
        masterBackground.setPercent(scrollPosition);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            String selectedItemName = drawerItems.get(position).getItemName();
            drawerLayout.closeDrawers();
            if(selectedItemName.equals(itemCurrentName))
            {
                openManualLocation();
                return;
            }
            if(selectedItemName.equals(item1Name))
            {
                openSearch();
                return;
            }
            if(selectedItemName.equals(item2Name))
            {
                openSettings();
                return;
            }
            if(selectedItemName.equals(item3Name))
            {
                helpAndFeedbackSelected();
                return;
            }

            // If it is none of the static choices, it has to be a city that is selected.
            // No need for any further checks :)
            citySelected(lats.get(position - 1), lons.get(position - 1));
        }
    }

    private void citySelected(String lat, String lon)
    {
        /*
        Start the CityWeather Activity with the stored lat and lon
         */
        Intent startCityWeatherActivity = new Intent(ActivityMain.this, ActivityCityWeather.class);
        Bundle bundle = new Bundle();
        bundle.putString(GlobalVariables.LATITUDE_KEY, lat);
        bundle.putString(GlobalVariables.LONGITUDE_KEY, lon);
        startCityWeatherActivity.putExtras(bundle);
        startActivity(startCityWeatherActivity);
    }

    private void setBackground()
    {
        Calendar cal = Calendar.getInstance();
        if(prefs.getValue(getString(R.string.HIGH_VISIBILITY_MODE), false))
        {
            masterBackground.setParallax(false);
            masterBackground.setBackgroundColor(0x212121);
        }
        else
        {
            masterBackground.setParallax(true);
            masterBackground.setBackground(getResources().getDrawable(ResourceHelper.getParallaxBackground(GlobalVariables.isDay(cal), weatherConditions)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return super.onCreateOptionsMenu(menu);
    }

    /*
    Uncomment this if you are using a swipe layout
    @Override
    public void onRefresh()
    {
        LoadData task = new LoadData();
        task.execute();
    }
    */

    /* LoadData is a great way of refreshing the data on all the tabs,
    Call it from onResume to get fresh data every time the page refreshes
     */
    /*
    public class LoadData extends AsyncTask<Void, Void, Void>
    {
		 * Start an async task that will run in the background
		 * Display the progressBar on pre-execute
		 * perform the async task
		 * Hide the progressBar and leave the activity once our task has been performed
		 * If there is an error loading databases or fetching data
		 * display the error messages here and exit after a couple of seconds (not implemented)

        @Override
        protected void onPreExecute()
        {
            // Disable further refreshes for now
            // swipeLayout.setEnabled(false);
            Toast.makeText(ActivityMain.this, StringDefinitions.REFRESH_STRING, Toast.LENGTH_SHORT).show();
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
            // swipeLayout.setEnabled(true);
            // swipeLayout.setRefreshing(false);

            if(responseCode.equals(WeatherFetcher.LOAD_SUCCESS))
            {
                // Refresh layouts here
                if(globalVars.getViewCreated(0))
                {
                    OverviewFragment fragOverview = (OverviewFragment)getSupportFragmentManager().findFragmentByTag(tags[0]);
                    fragOverview.refreshUI();
                }
                if(globalVars.getViewCreated(1))
                {
                    DetailsFragment fragDetails = (DetailsFragment)getSupportFragmentManager().findFragmentByTag(tags[1]);
                    fragDetails.refreshUI();
                }
                if(globalVars.getViewCreated(2))
                {
                    HourlyFragment fragHourly = (HourlyFragment)getSupportFragmentManager().findFragmentByTag(tags[2]);
                    fragHourly.refreshUI();
                }
                if(globalVars.getViewCreated(3))
                {
                    DailyFragment fragDaily = (DailyFragment)getSupportFragmentManager().findFragmentByTag(tags[3]);
                    fragDaily.refreshUI();
                }
            }
        }
    }

    public void asyncTaskPerform()
    {
		 * Starts a service that fetches weather data using HTTPGET
		 * The service then stores the data in the appropriate databases
		 * I am using a service to ensure robustness
		 * This can use LOAD_MODE_FORCE if required

        WeatherFetcher fetcher = new WeatherFetcher(ActivityMain.this);
        responseCode = fetcher.fetchAndUpdateData(WeatherFetcher.LOAD_MODE_LAZY);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Pass the event to the actionBarDrawerToggle, if it returns true, this means the app icon has handled the event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void openManualLocation()
    {
        Intent manualLocation = new Intent(ActivityMain.this, ActivityManualLocation.class);
        startActivityForResult(manualLocation, 1);
    }

    private void openSearch()
    {
        Intent startSearchActivityIntent = new Intent(ActivityMain.this, ActivityCityLookup.class);
        startActivity(startSearchActivityIntent);
    }

    private void openSettings()
    {
        Intent startSettingsMenuActivity = new Intent(ActivityMain.this, ActivitySettingsMenu.class);
        startActivity(startSettingsMenuActivity);
    }

    private void helpAndFeedbackSelected()
    {
        Intent helpActivity = new Intent(ActivityMain.this, ActivityHelp.class);
        startActivity(helpActivity);
    }

    private void startPlayback()
    {
        mediaPlayer = MediaPlayer.create(ActivityMain.this, ResourceHelper.getSoundFile(weatherConditions));
        mediaPlayer.start();
    }

    private void releaseAudio()
    {
        mediaPlayer.release();
        mediaPlayer = null;
    }


    @Override
    public void onResume()
    {
        super.onResume();

        // Adding a listener here for when the view is actually drawn in order to get the correct height and width of the view.
        ViewTreeObserver vto = masterBackground.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                setBackground();
                ViewTreeObserver obs = masterBackground.getViewTreeObserver();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                {
                    obs.removeGlobalOnLayoutListener(this);
                }
                else
                {
                    obs.removeOnGlobalLayoutListener(this);
                }
            }

        });

        if(prefs.getValue(getString(R.string.PLAY_SONIFICATIONS), true))
        {
            startPlayback();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(prefs.getValue(getString(R.string.PLAY_SONIFICATIONS), true))
        {
            releaseAudio();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                // Restart the application to force the location change
                Intent application = new Intent(ActivityMain.this, ActivityStartup.class);
                startActivity(application);
                finish();
            }
            if(resultCode == RESULT_CANCELED)
            {
                // Do nothing, we decided not to change location
                Toast.makeText(ActivityMain.this, "You can turn on automatic location updates from settings", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
