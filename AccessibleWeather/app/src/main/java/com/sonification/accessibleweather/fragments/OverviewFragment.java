package com.sonification.accessibleweather.fragments;

import java.util.Calendar;
import java.util.Locale;

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

public class OverviewFragment extends Fragment
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
     */

	public final static int TAB_NUMBER = 0;

	public Context context;
	
	TextView dateText;
	TextView locationText;
	TextView feelsLikeText;
	TextView chanceOfRainText;
	TextView cloudConditionText;
	RelativeLayout overviewLayout;
	
	String dayText;
	String day;
	String month;
	String city;
	String feelsLikeTemp;
	String chanceOfRain;
	String cloudConditions;
	String readOut;
	
	GlobalVariables globalVars = new GlobalVariables();
	
	PreferencesHelper prefs;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View rootView = inflater.inflate(R.layout.layout_overview, container, false);
        
        context = getActivity().getApplicationContext();
        prefs = new PreferencesHelper(context);
        
        dateText = (TextView)rootView.findViewById(R.id.dateText);
        locationText = (TextView)rootView.findViewById(R.id.locationText);
        feelsLikeText = (TextView)rootView.findViewById(R.id.feelsLikeText);
        chanceOfRainText = (TextView)rootView.findViewById(R.id.chanceOfRainText);
        cloudConditionText = (TextView)rootView.findViewById(R.id.cloudConditionText);
        
        overviewLayout = (RelativeLayout)rootView.findViewById(R.id.overviewLayout);
        
		globalVars.setViewCreated(TAB_NUMBER, true);
		
		// refreshUI() called from onStart();
		
        return rootView;
    }
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		globalVars.setViewCreated(TAB_NUMBER, false);
	}

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
            if(overviewLayout != null)
            {
                overviewLayout.setContentDescription(readOut);
                overviewLayout.requestFocus();
            }
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		refreshUI();

        //doAccessibilityEventsDelayed();
	}
	
	public void refreshUI()
	{
		String dbDate = "";
		String dbCity = "";
		String dbTemp = "";
		String dbPopDay = "";
		String dbPopNight = "";
		String dbWeather = "";

		DatabaseCachedWeather dbCache = new DatabaseCachedWeather(context);
		Cursor cacheCursor = dbCache.allRows();

        boolean isMetric = prefs.isMetric();

		if(cacheCursor.moveToFirst())
		{
			dbDate = cacheCursor.getString(1);
			dbCity = cacheCursor.getString(4);
			dbPopDay = cacheCursor.getString(20);
			dbPopNight = cacheCursor.getString(21);
			dbWeather = cacheCursor.getString(8);
			
			if(isMetric)
			{
				dbTemp = cacheCursor.getString(17);
			}
			else
			{
				dbTemp = cacheCursor.getString(16);
			}
		}

        if(dbTemp == null || dbTemp.equalsIgnoreCase("null") || dbTemp.equals(""))
        {
            dbTemp = "0";
        }
		double roundTemp = Double.parseDouble(dbTemp);
		dbTemp = Integer.toString((int)roundTemp);
		
		cacheCursor.close();
		dbCache.close();
		
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.YEAR, Integer.parseInt(dbDate.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(dbDate.substring(4, 6)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dbDate.substring(6, 8)));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dbDate.substring(8, 10)));
		cal.set(Calendar.MINUTE, Integer.parseInt(dbDate.substring(10, 12)));

		dayText = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
		day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
		city = dbCity;
		feelsLikeTemp = dbTemp;
		
		if(cal.get(Calendar.HOUR_OF_DAY) >= GlobalVariables.DAY_START && cal.get(Calendar.HOUR_OF_DAY) < GlobalVariables.NIGHT_START)
		{
			chanceOfRain = dbPopDay;
		}
		else
		{
			chanceOfRain = dbPopNight;
		}
		
		if(chanceOfRain == null || chanceOfRain.equalsIgnoreCase("null"))
		{
			chanceOfRain = "0";
		}
		
		cloudConditions = dbWeather;

        String units = StringDefinitions.GET_READOUT((int)roundTemp, StringDefinitions.UNIT_TYPE_TEMP, isMetric);
        String unitSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_TEMP, isMetric);

		readOut = dayText + ", " + day + " " + month + ". " + city + ". Feels like " + feelsLikeTemp + " " + units + ". " + "Chance of rain " + chanceOfRain + " percent. " + cloudConditions;
		dateText.setText(dayText + ", " + day + " " + month);
		locationText.setText(city);
		feelsLikeText.setText("Feels like " + feelsLikeTemp + " " + unitSymbol);
        feelsLikeText.setContentDescription("Feels like " + feelsLikeTemp + " " + units);
		chanceOfRainText.setText("Chance of rain " + chanceOfRain + "%");
		cloudConditionText.setText(cloudConditions);
	}
}
