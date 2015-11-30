package com.sonification.accessibleweather.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.fragments.DailyFragment;
import com.sonification.accessibleweather.fragments.DetailsFragment;
import com.sonification.accessibleweather.fragments.HourlyFragment;
import com.sonification.accessibleweather.fragments.OverviewFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
	/*
	 * This class extends FragmentPagerAdapter
	 * (Documentation - http://developer.android.com/reference/android/support/v13/app/FragmentPagerAdapter.html)
	 * TabsPagerAdapter extends and customizes the default android FragmentPagerAdapter,
	 * Specifies how to handle view changes for each tab of the application and which view to load
	 */
	
	public TabsPagerAdapter(FragmentManager fManager)
	{
		super(fManager);
	}

	@Override
	public Fragment getItem(int index)
	{
		/*
		 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
		 * getItem (specifies which fragment to return for each tab index)
		 * for a list of fragments, see the documentation
		 */
		
		switch(index)
        {
        case 0:
            return new OverviewFragment();
        case 1:
            return new DetailsFragment();
        case 2:
        	return new HourlyFragment();
        case 3:
        	return new DailyFragment();
        }
        return null;
	}

	@Override
	public int getCount()
	{
		return GlobalVariables.TOTAL_TABS;
	}
}
