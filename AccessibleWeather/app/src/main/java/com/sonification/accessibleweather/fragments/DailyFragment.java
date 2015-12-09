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
import com.sonification.accessibleweather.adapters.CustomDailyAdapter;
import com.sonification.accessibleweather.databases.DatabaseDailyWeather;
import com.sonification.accessibleweather.definitions.DailyItem;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.PreferencesHelper;
import com.sonification.accessibleweather.definitions.StringDefinitions;

import java.util.ArrayList;
import java.util.List;

public class DailyFragment extends Fragment
{
    /*
     * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
     * This is the class that contains everything pertaining to the Next 10 days forecast page.
     */

    private static final int TAB_NUMBER = 3;

    Context context;

    PreferencesHelper prefs;

    GlobalVariables globalVariables = new GlobalVariables();

    int numberOfInstances = 18;

    String[] day = new String[numberOfInstances];
    String[] month = new String[numberOfInstances];
    String[] year = new String[numberOfInstances];
    String[] text = new String[numberOfInstances];
    String[] pop = new String[numberOfInstances];
    String[] snow = new String[numberOfInstances];
    String[] highTemp = new String[numberOfInstances];
    String[] lowTemp = new String[numberOfInstances];
    String[] conditions = new String[numberOfInstances];

    List<DailyItem> dailyItems;

    ListView dailyListView;

    RelativeLayout dailyLayout;

    String temperatureSymbol;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View rootView = inflater.inflate(R.layout.layout_daily, container, false);

        context = getActivity().getApplicationContext();
        prefs = new PreferencesHelper(context);

        globalVariables.setViewCreated(TAB_NUMBER, true);

        dailyListView = (ListView)rootView.findViewById(R.id.dailyListView);
        dailyLayout = (RelativeLayout)rootView.findViewById(R.id.dailyLayout);

        boolean metric = prefs.isMetric();
        temperatureSymbol = StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_TEMP, metric);

        return rootView;
    }

    private String getItemString(int index)
    {
        String string = "High: " + highTemp[index] + StringDefinitions.UNICODE_DEGREE +
                          "\nLow: " + lowTemp[index] + StringDefinitions.UNICODE_DEGREE;

        if(snow[index] != null && !snow[index].equals("0"))
        {
            int roundSnow = (int)Double.parseDouble(snow[index]);
            string = string + "\nSnow: " + roundSnow;
        }

        string = string + "\nChance of rain: " + pop[index] + StringDefinitions.UNICODE_PERCENT;
        string = string + "\n" + conditions[index];

        return string;
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

        CustomDailyAdapter dailyAdapter = new CustomDailyAdapter(getActivity(), R.layout.rowlayout_daily, dailyItems);
        dailyListView.setAdapter(dailyAdapter);

        dailyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                createDialog(position);
            }
        });
    }

    private void createDialog(int position)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // We are creating text only for which items are selected to save on space
        alertDialogBuilder.setTitle(text[position] + " Forecast");
        alertDialogBuilder
                .setMessage(getItemString(position))
                .setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void loadValuesFromDatabase()
    {
        prefs = new PreferencesHelper(context);
        boolean metric = prefs.isMetric();

        DatabaseDailyWeather dbDaily = new DatabaseDailyWeather(context);
        Cursor dailyCursor = dbDaily.allRows();

        dailyItems = new ArrayList<>();

        if(dailyCursor.moveToFirst())
        {
            for(int i = 0; i < numberOfInstances; i++)
            {
                day[i] = dailyCursor.getString(1);
                month[i] = dailyCursor.getString(2);
                year[i] = dailyCursor.getString(3);
                text[i] = dailyCursor.getString(4);
                pop[i] = dailyCursor.getString(5);

                if (metric) {
                    snow[i] = dailyCursor.getString(7);
                    highTemp[i] = dailyCursor.getString(9);
                    lowTemp[i] = dailyCursor.getString(11);
                } else {
                    snow[i] = dailyCursor.getString(6);
                    highTemp[i] = dailyCursor.getString(8);
                    lowTemp[i] = dailyCursor.getString(10);
                }

                if(snow[i] != null && !snow[i].equals(""))
                {
                    // Rounding off snow to the nearest units
                    snow[i] = Integer.toString((int) Math.round(Double.parseDouble(snow[i])));
                }
                conditions[i] = dailyCursor.getString(12);

                dailyItems.add(new DailyItem(text[i],
                        conditions[i],
                        "Highs: " + highTemp[i] + StringDefinitions.UNICODE_DEGREE,
                        "Lows: " + lowTemp[i] + StringDefinitions.UNICODE_DEGREE,
                        "Chance of rain: " + pop[i] + StringDefinitions.UNICODE_PERCENT,
                        "Snow: " + snow[i] + " " + StringDefinitions.GET_SYMBOL(StringDefinitions.UNIT_TYPE_SNOW_HEIGHT, prefs.isMetric())));

                dailyCursor.moveToNext();
            }
        }

        dailyCursor.close();
        dbDaily.close();
    }
}
