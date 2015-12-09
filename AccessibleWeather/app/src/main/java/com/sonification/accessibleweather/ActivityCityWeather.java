package com.sonification.accessibleweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sonification.accessibleweather.adapters.CustomDailyAdapter;
import com.sonification.accessibleweather.definitions.DailyItem;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jehoshaph Akshay on 22 October 2014.
 * This activity loads and displays the weather and forecast for the given city
 */
public class ActivityCityWeather extends Activity
{
    ProgressBar cityWeatherLoadingBar;

    TextView cityLocationText;
    TextView cityTimeText;
    TextView cityTemperatureText;
    TextView cityPopText;
    TextView cityHumidityText;
    TextView cityWindText;

    ListView cityDailyList;

    int numberOfInstances = 18;

    PreferencesHelper prefs;

    String cityLocationString = "";
    String cityTimeString = "";
    String cityTemperatureString = "Temperature: ";
    String temperatureDescription = "Temperature: ";
    String cityPopString = "Chance of rain: ";
    String cityHumidityString = "Relative humidity: ";
    String cityWindString = "";
    String windDescription = "";

    String[] dayText = new String[numberOfInstances];
    String[] pop = new String[numberOfInstances];
    String[] snow = new String[numberOfInstances];
    String[] highTemp = new String[numberOfInstances];
    String[] lowTemp = new String[numberOfInstances];
    String[] conditions = new String[numberOfInstances];

    String reasonCode = "";
    String NO_INTERNET_CONNECTIVITY = "No internet connectivity";
    String SUCCESS = "Success";
    String NO_SERVICE_TO_THIS_LOCATION = "No service for this location";

    String speedSymbol;
    String temperatureSymbol;
    String heightSymbol;

    boolean metric = false;

    List<DailyItem> dailyItems;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        String latitude = bundle.getString(GlobalVariables.LATITUDE_KEY);
        String longitude = bundle.getString(GlobalVariables.LONGITUDE_KEY);

        setContentView(R.layout.layout_cityweather);

        cityWeatherLoadingBar = (ProgressBar)findViewById(R.id.cityWeatherLoadingBar);

        cityLocationText = (TextView)findViewById(R.id.cityLocationText);
        cityTimeText = (TextView)findViewById(R.id.cityTimeText);
        cityTemperatureText = (TextView)findViewById(R.id.cityTempText);
        cityPopText = (TextView)findViewById(R.id.cityPopText);
        cityHumidityText = (TextView)findViewById(R.id.cityHumidityText);
        cityWindText = (TextView)findViewById(R.id.cityWindText);

        cityDailyList = (ListView)findViewById(R.id.cityDailyList);

        prefs = new PreferencesHelper(ActivityCityWeather.this);
        metric = prefs.isMetric();

        CityWeatherLookup task = new CityWeatherLookup(latitude, longitude);

        speedSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SPEED, metric);
        temperatureSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_TEMP, metric);
        heightSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SNOW_HEIGHT, metric);
        task.execute();
    }

    public class CityWeatherLookup extends AsyncTask<Void, Void, Void>
    {
		/*
		 * Start an Async task that looks up the latitude and longitude for a given location and display a confirmation
		 * dialog that can start the get weather class
		 */

        String latitude;
        String longitude;

        boolean success = false;

        CityWeatherLookup(String lat, String lon)
        {
            latitude = lat;
            longitude = lon;
        }

        @Override
        protected void onPreExecute()
        {
            cityWeatherLoadingBar.setVisibility(ProgressBar.VISIBLE);
            cityWeatherLoadingBar.requestFocus();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            success = asyncTaskPerform(latitude, longitude);

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            cityWeatherLoadingBar.setVisibility(ProgressBar.GONE);
            updateUI(success);
        }
    }

    private boolean asyncTaskPerform(String latitude, String longitude)
    {
        /*
        Get values and store in global Strings
        Update UI can then use those String values to create the UI
        Don't forget that update UI should also set focus reasonably
         */

        if(!connectionExists())
        {
            reasonCode = NO_INTERNET_CONNECTIVITY;
            return false;
        }

        if(loadCurrentWeather(latitude, longitude) && loadDailyWeather(latitude, longitude))
        {
            reasonCode = SUCCESS;
            return true;
        }
        else
        {
            reasonCode = NO_SERVICE_TO_THIS_LOCATION;
            return false;
        }
    }

    private boolean loadCurrentWeather(String lat, String lon)
    {
        InputStream iS;
        String linkConditions = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
                + GlobalVariables.CONDITIONS_API_URL + "/" + lat + "," + lon + GlobalVariables.JSON_API_URL;

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
        catch (IOException e)
        {
            responseString = "E: " + e;
        }

        return parseCurrentString(responseString);
    }

    private boolean loadDailyWeather(String lat, String lon)
    {
        InputStream iS;
        String linkConditions = GlobalVariables.WUNDERGROUND_API_URL + GlobalVariables.WUNDERGROUND_API_KEY_URL
                + GlobalVariables.FORECAST_10_DAYS_API_URL + "/" + lat + "," + lon + GlobalVariables.JSON_API_URL;

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
        catch (IOException e)
        {
            responseString = "E: " + e;
        }

        return parseDailyString(responseString);
    }

    private boolean parseDailyString(String response)
    {
        int arrLength;
        String[] dTitle;
        String[] dPop;
        String[] dHigh;
        String[] dLow;
        String[] dConditions;
        String[] dSnow;

        try
        {
            JSONObject summaryObject = new JSONObject(response);

            JSONObject entries = summaryObject.getJSONObject(GlobalVariables.FORECAST_DAILY_KEY);

            JSONObject textForecast = entries.getJSONObject(GlobalVariables.TEXT_FORECAST_DAILY_KEY);
            JSONArray forecastDayArray = textForecast.getJSONArray(GlobalVariables.FORECAST_DAILY_ARRAY_KEY);

            arrLength = forecastDayArray.length();

            dTitle = new String[arrLength];
            dPop = new String[arrLength];
            dHigh = new String[arrLength];
            dLow = new String[arrLength];
            dConditions = new String[arrLength];
            dSnow = new String[arrLength];

            for(int i = 0; i < arrLength; i++)
            {
                JSONObject period = forecastDayArray.getJSONObject(i);
                dTitle[i] = period.getString(GlobalVariables.FORECAST_DAILY_TITLE_KEY);
                dPop[i] = period.getString(GlobalVariables.FORECAST_DAILY_POP_KEY);

                if(i == 0)
                {
                    cityPopString = cityPopString + dPop[i] + StringDefinitions.UNICODE_PERCENT;
                }
            }

            JSONObject simpleForecast = entries.getJSONObject(GlobalVariables.SIMPLE_FORECAST_DAILY_KEY);
            forecastDayArray = simpleForecast.getJSONArray(GlobalVariables.FORECAST_DAILY_ARRAY_KEY);

            int arrLength2 = forecastDayArray.length();	// Unfortunately arrLength here is arrLength / 2 of previous arrLength, see docs for JSON explanation

            for(int i = 0; i < arrLength2; i++)
            {
                JSONObject period = forecastDayArray.getJSONObject(i);

                JSONObject high = period.getJSONObject(GlobalVariables.FORECAST_DAILY_HIGH_KEY);
                if(metric)
                {
                    dHigh[i * 2] = high.getString(GlobalVariables.FORECAST_DAILY_CELCIUS_KEY);
                    dHigh[(i * 2) + 1] = dHigh[i * 2];
                }
                else
                {
                    dHigh[i * 2] = high.getString(GlobalVariables.FORECAST_DAILY_FAHRENHEIT_KEY);
                    dHigh[(i * 2) + 1] = dHigh[i * 2];
                }

                JSONObject low = period.getJSONObject(GlobalVariables.FORECAST_DAILY_LOW_KEY);
                if(metric)
                {
                    dLow[i * 2] = low.getString(GlobalVariables.FORECAST_DAILY_CELCIUS_KEY);
                    dLow[(i * 2) + 1] = dLow[i * 2];
                }
                else
                {
                    dLow[i * 2] = low.getString(GlobalVariables.FORECAST_DAILY_FAHRENHEIT_KEY);
                    dLow[(i * 2) + 1] = dLow[i * 2];
                }

                dConditions[i * 2] = period.getString(GlobalVariables.FORECAST_DAILY_CONDITIONS_KEY);
                dConditions[(i * 2) + 1] = dConditions[i * 2];

                JSONObject snowDay = period.getJSONObject(GlobalVariables.FORECAST_DAILY_SNOW_DAY_KEY);
                if(metric)
                {
                    dSnow[i * 2] = snowDay.getString(GlobalVariables.FORECAST_DAILY_CMS_KEY);
                    if(dSnow[i * 2] == null || dSnow[i * 2].equalsIgnoreCase("null"))
                    {
                        dSnow[i * 2] = "0.0";
                    }
                }
                else
                {
                    dSnow[i * 2] = snowDay.getString(GlobalVariables.FORECAST_DAILY_INCHES_KEY);
                    if(dSnow[i * 2] == null || dSnow[i * 2].equalsIgnoreCase("null"))
                    {
                        dSnow[i * 2] = "0.0";
                    }
                }

                JSONObject snowNight = period.getJSONObject(GlobalVariables.FORECAST_DAILY_SNOW_NIGHT_KEY);
                if(metric)
                {
                    dSnow[(i * 2) + 1] = snowNight.getString(GlobalVariables.FORECAST_DAILY_CMS_KEY);
                    if(dSnow[i * 2] == null || dSnow[i * 2].equalsIgnoreCase("null"))
                    {
                        dSnow[i * 2] = "0.0";
                    }
                }
                else
                {
                    dSnow[(i * 2) + 1] = snowNight.getString(GlobalVariables.FORECAST_DAILY_INCHES_KEY);
                    if(dSnow[i * 2] == null || dSnow[i * 2].equalsIgnoreCase("null"))
                    {
                        dSnow[i * 2] = "0.0";
                    }
                }
            }
        }
        catch(JSONException e)
        {
            return false;
        }

        dailyItems = new ArrayList<>();
        try
        {
            for(int i = 0; i < numberOfInstances; i++)
            {
                dayText[i] = dTitle[i];
                pop[i] = "Chance of rain: " + dPop[i] + StringDefinitions.UNICODE_PERCENT;
                snow[i] = "Snow: ";
                if(dSnow[i] != null && !dSnow[i].equals("null") && Double.parseDouble(dSnow[i]) != 0)
                {
                    snow[i] = snow[i] + dSnow[i];
                }
                else
                {
                    snow[i] = snow[i] + 0;
                }
                snow[i] = snow[i] + " " + heightSymbol;
                highTemp[i] = "Highs: " + dHigh[i] + temperatureSymbol;
                lowTemp[i] = "Lows: " + dLow[i] + temperatureSymbol;
                conditions[i] = dConditions[i];
                // Done loading the forecast into our local String arrays, initialize a DailyItem array
                dailyItems.add(new DailyItem(dayText[i],
                        conditions[i],
                        "Highs: " + dHigh[i] + StringDefinitions.UNICODE_DEGREE,
                        "Lows: " + dLow[i] + StringDefinitions.UNICODE_DEGREE,
                        pop[i],
                        snow[i]));
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            return false;
        }

        return true;
    }

    private boolean parseCurrentString(String response)
    {
        try
        {
            JSONObject summaryObject = new JSONObject(response);

            JSONObject entries = summaryObject.getJSONObject(GlobalVariables.CONDITIONS_OBSERVATION_KEY);
            JSONObject location = entries.getJSONObject(GlobalVariables.CONDITIONS_LOCATION_KEY);

            /*
            Getting the date of the remote location is convoluted.
            Simpler methods may exist.
            Get the remote location's time zone from wunderground's server.
            Get the local time zone and current time.
            Add remote time zone to local time.
            Subtract current time zone.
             */

            String offset = entries.getString(GlobalVariables.LOCAL_TIMEZONE_KEY);

            int hours = Integer.parseInt(offset.substring(1, 3));
            int minutes = Integer.parseInt(offset.substring(3, 5));
            String symbol = offset.substring(0, 1);

            Calendar calUTC = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            if(symbol.equals("+"))
            {
                calUTC.add(Calendar.HOUR_OF_DAY, hours);
                calUTC.add(Calendar.MINUTE, minutes);
            }
            else
            {
                calUTC.add(Calendar.HOUR_OF_DAY, -hours);
                calUTC.add(Calendar.MINUTE, -minutes);
            }

            cityTimeString = calUTC.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            cityTimeString = cityTimeString + " " + simpleDateFormat.format(calUTC.getTime());

            String city = location.getString(GlobalVariables.CITY_NAME_KEY);
            String country = location.getString(GlobalVariables.COUNTRY_NAME_KEY);

            cityLocationString = city + ", " + country;

            int temp;
            if(metric)
            {
                temp = (int)Double.parseDouble(entries.getString(GlobalVariables.TEMP_C_KEY));

            }
            else
            {
                temp = (int)Double.parseDouble(entries.getString(GlobalVariables.TEMP_F_KEY));
            }

            cityTemperatureString = cityTemperatureString + temp;

            temperatureDescription = cityTemperatureString;
            cityTemperatureString = cityTemperatureString + " " + temperatureSymbol;
            String temperatureUnit = StringDefinitions.GET_READOUT(temp, StringDefinitions.UNIT_TYPE_TEMP, metric);
            temperatureDescription = temperatureDescription + " " + temperatureUnit;

            cityHumidityString = cityHumidityString + entries.getString(GlobalVariables.RELATIVE_HUMIDITY_KEY);
            String windDir = entries.getString(GlobalVariables.WIND_DIR_KEY);
            String windSpeed;
            if(metric)
            {
                windSpeed = entries.getString(GlobalVariables.WIND_KPH_KEY);
            }
            else
            {
                windSpeed = entries.getString(GlobalVariables.WIND_MPH_KEY);
            }

            cityWindString = "Wind ";
            if(windDir.length() > 0)
            {
                cityWindString = cityWindString + StringDefinitions.GET_WIND_DESC(windDir) + " ";
            }
            cityWindString = cityWindString + "at " + windSpeed;
            String speedUnit = StringDefinitions.GET_READOUT((int)Double.parseDouble(windSpeed), StringDefinitions.UNIT_TYPE_SPEED, metric);
            windDescription = cityWindString + " " + speedUnit;
            cityWindString = cityWindString + " " + speedSymbol;
        }
        catch(JSONException e)
        {
            return false;
        }

        return true;
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

    private boolean connectionExists()
    {
		/*
		 * Checks if the weather underground URL is reachable from the application
		 * The URL may be unreachable for a number of reasons not only if the server is down.
		 * (For example: the URL is banned in the country or blocked by the network admin)
		 */
        if(isNetworkAvailable(ActivityCityWeather.this))
        {
            try
            {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(GlobalVariables.WUNDERGROUND_URL).openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.connect();
                return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK);
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

    private void updateUI(boolean success)
    {
        if(success)
        {
            cityLocationText.setText(cityLocationString);
            cityTimeText.setText(cityTimeString);
            cityTemperatureText.setText(cityTemperatureString);
            cityTemperatureText.setContentDescription(temperatureDescription);
            cityPopText.setText(cityPopString);
            cityHumidityText.setText(cityHumidityString);
            cityWindText.setText(cityWindString);
            cityWindText.setContentDescription(windDescription);

            CustomDailyAdapter adapter = new CustomDailyAdapter(ActivityCityWeather.this, R.layout.rowlayout_daily, dailyItems);
            cityDailyList.setAdapter(adapter);

            cityDailyList.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    createDialogFor(position);
                }
            });

            cityLocationText.setFocusable(true);
            cityLocationText.requestFocus();
        }
    }

    private void createDialogFor(int position)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCityWeather.this);

        alertDialogBuilder
                .setTitle(dayText[position])
                .setMessage(highTemp[position] + "\n" + lowTemp[position] + "\n" + snow[position] + "\n" + pop[position] + "\n" + conditions[position])
                .setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
