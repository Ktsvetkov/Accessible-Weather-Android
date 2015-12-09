package com.sonification.accessibleweather.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.sonification.accessibleweather.R;
import com.sonification.accessibleweather.adapters.CustomHourlyAdapter;
import com.sonification.accessibleweather.databases.DatabaseHourlyWeather;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.HourlyItem;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HourlyFragment extends Fragment
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
     */

    private static final int TAB_NUMBER = 2;

    Context context;

    PreferencesHelper prefs;

    GlobalVariables globalVariables = new GlobalVariables();

    String[] hour = new String[24];
    String[] day = new String[24];
    String[] month = new String[24];
    String[] year = new String[24];
    String[] temp = new String[24];
    String[] feelsLikeTemp = new String[24];
    String[] pop = new String[24];
    String[] windSpeed = new String[24];
    String[] conditions = new String[24];
    String[] displayDate = new String[24];

    // Used to store the hourlyDetails
    List<HourlyItem> hourlyItems;

    ListView hourlyListView;

    RelativeLayout hourlyLayout;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View rootView = inflater.inflate(R.layout.layout_hourly, container, false);
        
        context = getActivity().getApplicationContext();
        prefs = new PreferencesHelper(context);

        globalVariables.setViewCreated(TAB_NUMBER, true);

        hourlyListView = (ListView)rootView.findViewById(R.id.hourlyListView);
        hourlyLayout = (RelativeLayout)rootView.findViewById(R.id.hourlyLayout);

        return rootView;
    }

    private String getItemString(int index)
    {
        return "Temperature: " + temp[index] + StringDefinitions.UNICODE_DEGREE
                + "\nFeels like: " + feelsLikeTemp[index] + StringDefinitions.UNICODE_DEGREE
                + "\nChance of rain: " + pop[index] + StringDefinitions.UNICODE_PERCENT + "\n"
                + "\nWind: " + windSpeed[index] + " " + StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SPEED, prefs.isMetric())
                + "\n" + conditions[index];
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        globalVariables.setViewCreated(TAB_NUMBER, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        refreshUI();
    }

    public void refreshUI()
    {
        loadValuesFromDatabase();

        setValuesInList();  // Responsible for populating the hourlyItems that draws the list

        CustomHourlyAdapter hourlyAdapter = new CustomHourlyAdapter(getActivity(), R.layout.rowlayout_hours, hourlyItems);
        hourlyListView.setAdapter(hourlyAdapter);

        hourlyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createDialog(position);
            }
        });
    }

    private void createDialog(int position)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // We are creating text only for which items are selected to save on space
        alertDialogBuilder.setTitle(displayDate[position] + " forecast");
        alertDialogBuilder
                .setMessage(getItemString(position))
                .setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void loadValuesFromDatabase()
    {
        boolean isMetric = prefs.isMetric();

        DatabaseHourlyWeather dbHourly = new DatabaseHourlyWeather(context);
        Cursor hourlyCursor = dbHourly.allRows();

        if(hourlyCursor.moveToFirst())
        {
            if (isMetric) {
                for (int i = 0; i < 24; i++) {
                    hour[i] = hourlyCursor.getString(1);
                    day[i] = hourlyCursor.getString(2);
                    month[i] = hourlyCursor.getString(3);
                    year[i] = hourlyCursor.getString(4);
                    temp[i] = hourlyCursor.getString(6);
                    conditions[i] = hourlyCursor.getString(7);
                    windSpeed[i] = hourlyCursor.getString(9);
                    feelsLikeTemp[i] = hourlyCursor.getString(11);
                    pop[i] = hourlyCursor.getString(12);
                    hourlyCursor.moveToNext();
                }
            } else {
                for (int i = 0; i < 24; i++) {
                    hour[i] = hourlyCursor.getString(1);
                    day[i] = hourlyCursor.getString(2);
                    month[i] = hourlyCursor.getString(3);
                    year[i] = hourlyCursor.getString(4);
                    temp[i] = hourlyCursor.getString(5);
                    conditions[i] = hourlyCursor.getString(7);
                    windSpeed[i] = hourlyCursor.getString(8);
                    feelsLikeTemp[i] = hourlyCursor.getString(10);
                    pop[i] = hourlyCursor.getString(12);
                    hourlyCursor.moveToNext();
                }
            }
        }
        hourlyCursor.close();
        dbHourly.close();
    }

    private void setValuesInList()
    {
        hourlyItems = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for(int i = 0; i < 24; i++)
        {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[i]));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day[i]));
            cal.set(Calendar.MONTH, (Integer.parseInt(month[i]) - 1));
            cal.set(Calendar.YEAR, Integer.parseInt(year[i]));

            String append = "am";
            int hour = cal.get(Calendar.HOUR);

            if(cal.get(Calendar.AM_PM) == 1)
            {
                append = "pm";
            }
            if(hour == 0)
            {
                hour = 12;
            }
            displayDate[i] = hour + " " + append;

            hourlyItems.add(new HourlyItem(displayDate[i],
                    conditions[i],
                    "Temperature: " + temp[i] + StringDefinitions.UNICODE_DEGREE + " (" + feelsLikeTemp[i] + StringDefinitions.UNICODE_DEGREE + ")",
                    "Chance of rain: " + pop[i] + StringDefinitions.UNICODE_PERCENT,
                    cal.get(Calendar.HOUR_OF_DAY)));
        }
    }
}