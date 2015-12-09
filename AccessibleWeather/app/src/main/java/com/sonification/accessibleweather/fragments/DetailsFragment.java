package com.sonification.accessibleweather.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sonification.accessibleweather.R;
import com.sonification.accessibleweather.databases.DatabaseCachedWeather;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;

import java.util.Calendar;

public class DetailsFragment extends Fragment
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
     */

	public final static int TAB_NUMBER = 1;
	
	public Context context;
	
	TextView detailsTemperatureText;
	TextView detailsHumidityText;
	TextView detailsWindStringText;
	TextView detailsPopDayText;
	TextView detailsPopNightText;
	TextView detailsVisibilityText;
	TextView detailsPrecipitationText;
    TextView detailsSunriseText;
    TextView detailsSunsetText;
	
	RelativeLayout detailsLayout;
	
	PreferencesHelper prefs;
	
	GlobalVariables globalVars = new GlobalVariables();
	
	String readOut = "";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View rootView = inflater.inflate(R.layout.layout_details, container, false);
        
        context = getActivity().getApplicationContext();
        prefs = new PreferencesHelper(context);
        
        detailsTemperatureText = (TextView)rootView.findViewById(R.id.detailsTemperatureText);
        detailsHumidityText = (TextView)rootView.findViewById(R.id.detailsHumidityText);
        detailsWindStringText = (TextView)rootView.findViewById(R.id.detailsWindStringText);
        detailsPopDayText = (TextView)rootView.findViewById(R.id.detailsPopDayText);
        detailsPopNightText = (TextView)rootView.findViewById(R.id.detailsPopNightText);
        detailsVisibilityText = (TextView)rootView.findViewById(R.id.detailsVisibilityText);
        detailsPrecipitationText = (TextView)rootView.findViewById(R.id.detailsPrecipitationText);
        detailsSunsetText = (TextView)rootView.findViewById(R.id.detailsSunsetText);
        detailsSunriseText = (TextView)rootView.findViewById(R.id.detailsSunriseText);
        
        detailsLayout = (RelativeLayout)rootView.findViewById(R.id.detailsLayout);
        
        globalVars.setViewCreated(TAB_NUMBER, true);
        
        return rootView;
    }
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		globalVars.setViewCreated(TAB_NUMBER, false);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		refreshUI();
	}
	
	/*@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser)
		{
			// Fragment now visible, this is useful to know when to call accessibility for this tab
			doAccessibilityEvents();
		}
	}*/

    @Override
    public void setMenuVisibility(final boolean visible)
    {
        super.setMenuVisibility(visible);
        if (visible)
        {
            // Fragment now visible, this is useful to know when to call accessibility for this tab
            doAccessibilityEvents();
        }
    }
	
	public void doAccessibilityEvents()
	{
		if(globalVars.getCurrentTab() == TAB_NUMBER && globalVars.getViewCreated(TAB_NUMBER))
		{
			detailsLayout.setContentDescription(readOut);
			detailsLayout.requestFocus();
		}
	}
	
	public void refreshUI()
	{
		String dbTemp = "";
		String dbHumidity = "";
		String dbWindDir = "";
		String dbWindSpeed = "";
		String dbPopDay = "";
		String dbPopNight = "";
		String dbPrecipitation = "";
		String dbVisibility = "";
        String dbSunrise = "";
        String dbSunset = "";

        String temperature;
        String humidity;
        String wind;
        String dayPop;
        String nightPop;
        String visibility;
        String precipitation;
        String sunrise;
        String sunset;

        String temperatureDescription;
        String visibilityDescription;
        String windDescription;
        String precipitationDescription;

		DatabaseCachedWeather dbCache = new DatabaseCachedWeather(context);
		Cursor cacheCursor = dbCache.allRows();

        boolean metric = prefs.isMetric();
		
		if(cacheCursor.moveToFirst())
		{
			if(metric)
			{
				dbTemp = cacheCursor.getString(10);
				dbWindSpeed = cacheCursor.getString(15);
				dbPrecipitation = cacheCursor.getString(23);
				dbVisibility = cacheCursor.getString(19);
			}
			else
			{
				dbTemp = cacheCursor.getString(9);
				dbWindSpeed = cacheCursor.getString(14);
				dbPrecipitation = cacheCursor.getString(22);
				dbVisibility = cacheCursor.getString(18);
			}
			
			dbHumidity = cacheCursor.getString(11);
			dbWindDir = cacheCursor.getString(13);
			dbPopDay = cacheCursor.getString(20);
			dbPopNight = cacheCursor.getString(21);
            dbSunrise = cacheCursor.getString(26);
            dbSunset = cacheCursor.getString(27);
		}
		
		cacheCursor.close();
		dbCache.close();

        // Rounding all to the nearest integer
        double rounderTemp;
        try
        {
            rounderTemp = Double.parseDouble(dbTemp);
        }
        catch(Exception e)
        {
            rounderTemp= 0.0;
        }
        dbTemp = Integer.toString((int)rounderTemp);

        double rounderVisibility;
        try
        {
            rounderVisibility = Double.parseDouble(dbVisibility);
        }
        catch(Exception e)
        {
            rounderVisibility = 0.0;
        }
        dbVisibility = Integer.toString((int)rounderVisibility);

        double rounderPPT;
        try
        {
            rounderPPT = Double.parseDouble(dbPrecipitation);
        }
        catch(Exception e)
        {
            rounderPPT = 0.0;
        }
        dbPrecipitation = Integer.toString((int)rounderPPT);

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

        String tempUnits = StringDefinitions.GET_READOUT((int)rounderTemp, StringDefinitions.UNIT_TYPE_TEMP, metric);
        String tempUnitSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_TEMP, metric);

        String speedUnits = StringDefinitions.GET_READOUT((int)rounderWind, StringDefinitions.UNIT_TYPE_SPEED, metric);
        String speedUnitSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SPEED, metric);

        String visibilityUnits = StringDefinitions.GET_READOUT((int)rounderVisibility, StringDefinitions.UNIT_TYPE_DISTANCE, metric);
        String visibilityUnitSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_DISTANCE, metric);

        String heightUnits = StringDefinitions.GET_READOUT(2, StringDefinitions.UNIT_TYPE_HEIGHT, metric);
        String heightUnitSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_HEIGHT, metric);

		temperature = "Temperature: " + dbTemp + " " + tempUnitSymbol;
        temperatureDescription = "Temperature " + dbTemp + " " + tempUnits;

        humidity = "Relative humidity: " + dbHumidity;
		
		wind = "Wind ";
        if(dbWindDir.length() > 0)
        {
            wind = wind + StringDefinitions.GET_WIND_DESC(dbWindDir) + " ";
        }
	    wind = wind + "at " + dbWindSpeed + " ";
        windDescription = wind + " " + speedUnits;
		wind = wind + speedUnitSymbol;

        dayPop = "Day: " + dbPopDay + StringDefinitions.UNICODE_PERCENT;
		nightPop = "Night: " + dbPopNight + StringDefinitions.UNICODE_PERCENT;

		visibility = "Visibility: " + dbVisibility + " " + visibilityUnitSymbol;
        visibilityDescription = "Visibility " + dbVisibility + " " + visibilityUnits;

		precipitation = "Precipitation: " + dbPrecipitation + " " + heightUnitSymbol;
        precipitationDescription = "Precipitation " + dbPrecipitation + " " + heightUnits;

        sunrise = get12HourString(dbSunrise);
        sunset = get12HourString(dbSunset);
        sunrise = "Sunrise at " + sunrise;
        sunset = "Sunset at " + sunset;

		detailsTemperatureText.setText(temperature);
        detailsTemperatureText.setContentDescription(temperatureDescription);

        detailsHumidityText.setText(humidity);
		detailsHumidityText.setContentDescription(humidity);

        detailsWindStringText.setText(wind);
        detailsWindStringText.setContentDescription(windDescription);

        detailsPopDayText.setText(dayPop);
		detailsPopNightText.setText(nightPop);

        detailsVisibilityText.setText(visibility);
        detailsVisibilityText.setContentDescription(visibilityDescription);
		detailsPrecipitationText.setText(precipitation);
        detailsPrecipitationText.setContentDescription(precipitationDescription);
        detailsSunriseText.setText(sunrise);
        detailsSunriseText.setContentDescription(sunrise);
        detailsSunsetText.setText(sunset);
        detailsSunsetText.setContentDescription(sunset);

        readOut = temperatureDescription + ". " + humidity + ". " + windDescription + ". Chance of rain, " + dayPop + ", " + nightPop
                + ". " + precipitationDescription + ". " + visibilityDescription + ". " + sunrise + ". " + sunset;

        detailsLayout.setContentDescription(readOut);
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
}