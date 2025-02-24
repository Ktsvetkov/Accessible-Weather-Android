package com.sonification.accessibleweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sonification.accessibleweather.databases.DatabasePreviousCities;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.StringDefinitions;

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
import java.net.URLEncoder;

public class ActivityCityLookup extends Activity
{
    /*
    Class provides a simple interface in which to search for a city
    or select a previous city
    Created by Jehoshaph Akshay 22/10/2014
     */
    ProgressBar locationLookupProgressBar;
    TextView previousSearchesText;
    EditText citySearchEdit;
    ListView previousCitiesList;
    Button goButton;

    String foundCityName;
    String foundCountryName;
    String foundLat;
    String foundLon;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPage();
    }

    private void loadPage()
    {
        setContentView(R.layout.layout_citysearch);

        previousSearchesText = (TextView)findViewById(R.id.previousSearchesText);
        citySearchEdit = (EditText)findViewById(R.id.citySearchEdit);
        previousCitiesList = (ListView)findViewById(R.id.previousCitiesList);
        goButton = (Button)findViewById(R.id.goButton);
        locationLookupProgressBar = (ProgressBar)findViewById(R.id.locationLookupProgressBar);

        goButton.setEnabled(false);
        if(locationLookupProgressBar.getVisibility() == ProgressBar.VISIBLE)
        {
            locationLookupProgressBar.setVisibility(ProgressBar.GONE);
        }

        /*
        Edit Text changed listener that affects the click ability of the button
         */
        citySearchEdit.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if(s.length() > 0)
                {
                    goButton.setEnabled(true);
                }
                else
                {
                    goButton.setEnabled(false);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }
        });

        /*
        Add go in built button listener on edit text
         */
        citySearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_GO)
                {
                    fetchLocation();
                    return true;
                }
                return false;
            }
        });

        goButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLocation();
            }
        });

        refreshUI();
    }

    private void refreshUI()
    {
        /*
        set list view and list view listener here
         */
        DatabasePreviousCities dbPrevCities = new DatabasePreviousCities(ActivityCityLookup.this);
        Cursor prevCitiesCursor = dbPrevCities.allRows();
        int rowCount = (int)dbPrevCities.count();
        String[] cities = new String[rowCount];
        String[] countries = new String[rowCount];
        final String[] latitudes = new String[rowCount];
        final String[] longitudes = new String[rowCount];
        String[] combined = new String[rowCount];

        if(prevCitiesCursor.moveToFirst())
        {
            int count = 0;
            while(!prevCitiesCursor.isAfterLast())
            {
                cities[count] = prevCitiesCursor.getString(1);
                countries[count] = prevCitiesCursor.getString(2);
                latitudes[count] = prevCitiesCursor.getString(3);
                longitudes[count] = prevCitiesCursor.getString(4);
                combined[count] = cities[count] + ", " + countries[count];

                prevCitiesCursor.moveToNext();
                count++;
            }
        }

        if(rowCount == 0)
        {
            previousSearchesText.setText(StringDefinitions.LOCATION_SEARCH_STRING);
        }
        else
        {
            previousSearchesText.setText(StringDefinitions.PREVIOUS_SEARCH_STRING);

            // Setting list view here
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityCityLookup.this, R.layout.layout_listheader, R.id.headerText, combined);
            previousCitiesList.setAdapter(adapter);

            previousCitiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (internetConnection()) {
                        getWeather(latitudes[position], longitudes[position]);
                    } else {
                        Toast.makeText(ActivityCityLookup.this, getResources().getText(R.string.noInternetString), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        prevCitiesCursor.close();
        dbPrevCities.close();
    }

    private boolean internetConnection() {
        /*
		 * Checks if the device has an internet connection
		 * or is connected to a wifi network
		 */
        ConnectivityManager connectivityManager = (ConnectivityManager)ActivityCityLookup.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void fetchLocation()
    {
        String lookupLocation = citySearchEdit.getText().toString();

        if(lookupLocation != null && !lookupLocation.equals(""))
        {
            getLocation(lookupLocation);
        }
        else
        {
            Toast.makeText(ActivityCityLookup.this, StringDefinitions.LOOKUP_FAILED_STRING, Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation(String lookupLocation)
    {
        /*
        Gets the location in lat, lon and then calls getWeather
         */
        LocationLookup task = new LocationLookup(lookupLocation);
        task.execute();
    }

    private void getWeather(String lat, String lon)
    {
        /*
        Start the next activity that shows the current
        weather at that place and lets the user select weather for the next few days.
        Weather is loaded in an async task and then displayed.
         */
        Intent startCityWeatherActivity = new Intent(ActivityCityLookup.this, ActivityCityWeather.class);
        Bundle bundle = new Bundle();
        bundle.putString(GlobalVariables.LATITUDE_KEY, lat);
        bundle.putString(GlobalVariables.LONGITUDE_KEY, lon);
        startCityWeatherActivity.putExtras(bundle);
        startActivity(startCityWeatherActivity);
    }

    public class LocationLookup extends AsyncTask<Void, Void, Void>
    {
		/*
		 * Start an Async task that looks up the latitude and longitude for a given location and display a confirmation
		 * dialog that can start the get weather class
		 */

        String location;
        boolean success = false;

        LocationLookup(String lookupLocation)
        {
            location = lookupLocation;
        }

        @Override
        protected void onPreExecute()
        {
            locationLookupProgressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            success = asyncTaskPerform(location);

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            locationLookupProgressBar.setVisibility(ProgressBar.GONE);
            displayConfirmationDialog(success);
        }
    }

    private boolean asyncTaskPerform(String location)
    {
        InputStream iS;
        try
        {
            location = URLEncoder.encode(location, GlobalVariables.CHARACTER_SET);
        }
        catch (Exception e)
        {
            // Unsupported encoding
            return false;
        }
        String linkGeoLookup = GlobalVariables.GEONAMES_SEARCH1_URL + location + GlobalVariables.GEONAMES_SEARCH2_URL;
        String responseString;

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(linkGeoLookup);
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

        return parseGeoLookup(responseString);
    }

    private boolean parseGeoLookup(String responseString)
    {
        try
        {
            JSONObject summaryObject = new JSONObject(responseString);

            int numberOfResults = Integer.parseInt(summaryObject.getString(GlobalVariables.GEO_RESULTS_KEY));

            if(numberOfResults == 0)
            {
                return false;
            }

            JSONArray results = summaryObject.getJSONArray(GlobalVariables.GEONAMES_ARRAY_KEY);
            JSONObject result1 = results.getJSONObject(0);

            foundCityName = result1.getString(GlobalVariables.GEO_CITY_NAME_KEY);
            foundCountryName = result1.getString(GlobalVariables.GEO_COUNTRY_NAME_KEY);
            foundLat = result1.getString(GlobalVariables.GEO_LAT_KEY);
            foundLon = result1.getString(GlobalVariables.GEO_LON_KEY);

            return true;
        }
        catch(JSONException e)
        {
            return false;
        }
        catch(Exception e)
        {
            return false;
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

    private void displayConfirmationDialog(boolean success)
    {
        if(success)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCityLookup.this);

            alertDialogBuilder
                    .setMessage("Do you mean:\n" + foundCityName + "\n" + foundCountryName)
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            yesGetWeather();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            noResearch();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();
        }
        else
        {
            Toast.makeText(ActivityCityLookup.this, StringDefinitions.LOOKUP_FAILED_STRING, Toast.LENGTH_SHORT).show();
            citySearchEdit.requestFocus();
        }
    }

    private void noResearch()
    {
        citySearchEdit.setFocusable(true);
        citySearchEdit.requestFocus();
    }

    private void yesGetWeather()
    {
        DatabasePreviousCities dbPrev = new DatabasePreviousCities(ActivityCityLookup.this);
        Cursor existingCursor = dbPrev.searchRows(DatabasePreviousCities.CITY_NAME, foundCityName);
        if(existingCursor.moveToFirst())
        {
            int locID = existingCursor.getInt(0);
            dbPrev.deleteEntry(locID);
        }
        existingCursor.close();
        dbPrev.addRecord(foundCityName, foundCountryName, foundLat, foundLon);
        dbPrev.close();
        refreshUI();
        getWeather(foundLat, foundLon);
    }
}