package com.sonification.accessibleweather.definitions;

/*
Utility class to return different backgrounds/images/sounds depending on the given parameters
Do not define any resources anywhere else in the application, make all declarations here
and use getter methods to return the resources from this class.
If you find yourself importing packages like bitmaps etc. to this class, you may have gone wrong.
Written by Jehoshaph Akshay Chandran (11 Jan 2015)
 */

import com.sonification.accessibleweather.R;

import java.util.Random;

public class ResourceHelper
{
    /*
    Method to return a drawable to use with a row layout based on the conditions
     */
    public static int getHourlyIcon(String conditions, int time)
    {
        if(GlobalVariables.isDay(time))
        {
            switch(StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.mostlysunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.partlysunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.fog_hazy;
            }
            return R.drawable.mostlysunny;
        }
        else
        {
            switch (StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.nt_sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.nt_mostlysunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.nt_partlysunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.nt_overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.nt_rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.nt_sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.nt_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.nt_snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.nt_tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.nt_fog_hazy;
            }
            return R.drawable.nt_mostlysunny;
        }
    }

    /*
    Method to return a small notification icon resource id based on the conditions
     */
    public static int getSmallIcon(String conditions, int time)
    {
        if(GlobalVariables.isDay(time))
        {
            switch(StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.not_sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.not_mostly_sunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.not_partly_sunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.not_overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.not_rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.not_sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.not_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.not_snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.not_tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.not_fog_hazy;
            }
            return R.drawable.not_mostly_sunny;
        }
        else
        {
            switch (StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.not_nt_sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.not_nt_mostly_sunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.not_nt_partly_sunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.not_nt_overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.not_nt_rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.not_nt_sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.not_nt_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.not_nt_snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.not_nt_tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.not_nt_fog_hazy;
            }
            return R.drawable.not_nt_mostly_sunny;
        }
    }

    /*
    Method to return a large icon resource id based on conditions
     */
    public static int getLargeIcon(String conditions, int time)
    {
        if(GlobalVariables.isDay(time))
        {
            switch(StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.mostlysunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.partlysunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.fog_hazy;
            }
            return R.drawable.mostlysunny;
        }
        else
        {
            switch (StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    return R.drawable.nt_sunny;
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                    return R.drawable.nt_mostlysunny;
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    return R.drawable.nt_partlysunny;
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    return R.drawable.nt_overcast;
                case StringDefinitions.CONDITION_RAIN_KEY:
                    return R.drawable.nt_rain;
                case StringDefinitions.CONDITION_SLEET_KEY:
                    return R.drawable.nt_sleet;
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.nt_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    return R.drawable.nt_snow;
                case StringDefinitions.CONDITION_STORM_KEY:
                    return R.drawable.nt_tstorms;
                case StringDefinitions.CONDITION_FOG_KEY:
                case StringDefinitions.CONDITION_HAZE_KEY:
                    return R.drawable.nt_fog_hazy;
            }
            return R.drawable.nt_mostlysunny;
        }
    }

    /*
    Method to return a large parallax background resource id based on conditions
     */
    public static int getParallaxBackground(boolean isDay, String conditions)
    {
        Random rand = new Random();
        int randNum;

        // First check for all the common conditions
        switch(StringDefinitions.GET_CONDITION(conditions))
        {
            case StringDefinitions.CONDITION_FOG_KEY:
            case StringDefinitions.CONDITION_HAZE_KEY:
                randNum = rand.nextInt(2);
                if(randNum == 0)
                {
                    return R.drawable.bg_fog01;
                }
                else
                {
                    return R.drawable.bg_fog02;
                }
            case StringDefinitions.CONDITION_RAIN_KEY:
                randNum = rand.nextInt(3);
                if(randNum == 0)
                {
                    return R.drawable.bg_rain01;
                }
                else if(randNum == 1)
                {
                    return R.drawable.bg_rain02;
                }
                else
                {
                    return R.drawable.bg_rain03;
                }
            case StringDefinitions.CONDITION_SLEET_KEY:
                return R.drawable.bg_sleet;
            case StringDefinitions.CONDITION_STORM_KEY:
                randNum = rand.nextInt(2);
                if(randNum == 0)
                {
                    return R.drawable.bg_tstorm01;
                }
                else
                {
                    return R.drawable.bg_tstorm02;
                }
        }

        // Now check for time-dependent conditions
        if(isDay)
        {
            switch(StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    randNum = rand.nextInt(3);
                    if(randNum == 0)
                    {
                        return R.drawable.bg_clear01;
                    }
                    else if(randNum == 1)
                    {
                        return R.drawable.bg_clear02;
                    }
                    else
                    {
                        return R.drawable.bg_clear03;
                    }
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    randNum = rand.nextInt(3);
                    if(randNum == 0)
                    {
                        return R.drawable.bg_cloudy01;
                    }
                    else if(randNum == 1)
                    {
                        return R.drawable.bg_cloudy02;
                    }
                    else
                    {
                        return R.drawable.bg_cloudy03;
                    }
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    randNum = rand.nextInt(2);
                    if(randNum == 0)
                    {
                        return R.drawable.bg_overcast01;
                    }
                    else
                    {
                        return R.drawable.bg_overcast02;
                    }
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.bg_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    randNum = rand.nextInt(2);
                    if(randNum == 0)
                    {
                        return R.drawable.bg_snow01;
                    }
                    else
                    {
                        return R.drawable.bg_snow02;
                    }
            }
            return R.drawable.bg_cloudy01;
        }
        else
        {
            switch(StringDefinitions.GET_CONDITION(conditions))
            {
                case StringDefinitions.CONDITION_CLEAR_KEY:
                    randNum = rand.nextInt(3);
                    if(randNum == 0)
                    {
                        return R.drawable.nt_bg_clear01;
                    }
                    else if(randNum == 1)
                    {
                        return R.drawable.nt_bg_clear02;
                    }
                    else
                    {
                        return R.drawable.nt_bg_clear03;
                    }
                case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
                    randNum = rand.nextInt(3);
                    if(randNum == 0)
                    {
                        return R.drawable.nt_bg_cloudy01;
                    }
                    else if(randNum == 1)
                    {
                        return R.drawable.nt_bg_cloudy02;
                    }
                    else
                    {
                        return R.drawable.nt_bg_cloudy03;
                    }
                case StringDefinitions.CONDITION_OVERCAST_KEY:
                    randNum = rand.nextInt(2);
                    if(randNum == 0)
                    {
                        return R.drawable.nt_bg_overcast01;
                    }
                    else
                    {
                        return R.drawable.nt_bg_overcast02;
                    }
                case StringDefinitions.CONDITION_FLURRIES_KEY:
                    return R.drawable.nt_bg_flurries;
                case StringDefinitions.CONDITION_SNOW_KEY:
                    randNum = rand.nextInt(2);
                    if(randNum == 0)
                    {
                        return R.drawable.nt_bg_snow01;
                    }
                    else
                    {
                        return R.drawable.nt_bg_snow02;
                    }
            }
            return R.drawable.nt_bg_cloudy01;
        }
    }

    /*
    Method to return which theme to use (i.e. light or dark theme)
     */
    public static int getAppTheme(boolean isDay, String conditions)
    {
        // First check for all the common conditions
        switch(StringDefinitions.GET_CONDITION(conditions))
        {
            case StringDefinitions.CONDITION_RAIN_KEY:
                return R.style.DarkTheme;
            case StringDefinitions.CONDITION_FOG_KEY:
            case StringDefinitions.CONDITION_HAZE_KEY:
            case StringDefinitions.CONDITION_SLEET_KEY:
            case StringDefinitions.CONDITION_STORM_KEY:
                return R.style.LightTheme;
        }

        // Now check for time-dependent conditions
        if(isDay)
        {
            return R.style.LightTheme;
        }
        else
        {
            return R.style.DarkTheme;
        }
    }

    /*
    Method to return a sound resource id based on conditions
     */
    public static int getSoundFile(String conditions)
    {
        switch(StringDefinitions.GET_CONDITION(conditions))
        {
            case StringDefinitions.CONDITION_CLEAR_KEY:
            case StringDefinitions.CONDITION_PARTLY_CLOUDY_KEY:
                return R.raw.mp3_clear;
            case StringDefinitions.CONDITION_MOSTLY_CLOUDY_KEY:
            case StringDefinitions.CONDITION_OVERCAST_KEY:
                return R.raw.mp3_cloud;
            case StringDefinitions.CONDITION_FOG_KEY:
            case StringDefinitions.CONDITION_HAZE_KEY:
                return R.raw.mp3_fog;
            case StringDefinitions.CONDITION_SLEET_KEY:
                return R.raw.mp3_ice_sleet;
            case StringDefinitions.CONDITION_RAIN_KEY:
                return R.raw.mp3_rain;
            case StringDefinitions.CONDITION_STORM_KEY:
                return R.raw.mp3_rain_storm;
            case StringDefinitions.CONDITION_SNOW_KEY:
                return R.raw.mp3_snow;
            case StringDefinitions.CONDITION_FLURRIES_KEY:
                return R.raw.mp3_snow_flurries;
        }
        return R.raw.mp3_cloud;
    }
}
