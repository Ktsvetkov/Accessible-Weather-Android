package com.sonification.accessibleweather.definitions;

public class StringDefinitions
{
	/*
	 * Written by Jehoshaph Akshay Chandran (2014) at the Sonification Lab, Georgia Institute of Technology
	 * Class to store string definitions and other universal values
	 * These values may be accessed from anywhere else in the application
	 */
    // Conditions keys
    public static final int CONDITION_CLEAR_KEY = 0;
    public static final int CONDITION_PARTLY_CLOUDY_KEY = 1;
    public static final int CONDITION_MOSTLY_CLOUDY_KEY = 2;
    public static final int CONDITION_OVERCAST_KEY = 3;
    public static final int CONDITION_RAIN_KEY = 4;
    public static final int CONDITION_SLEET_KEY = 5;
    public static final int CONDITION_FLURRIES_KEY = 6;
    public static final int CONDITION_SNOW_KEY = 7;
    public static final int CONDITION_STORM_KEY = 8;
    public static final int CONDITION_FOG_KEY = 9;
    public static final int CONDITION_HAZE_KEY = 10;
    public static final int CONDITION_DEFAULT_KEY = CONDITION_PARTLY_CLOUDY_KEY;
	
	public static final String TAB0 = "Weather today";
	public static final String TAB1 = "Detailed";
	public static final String TAB2 = "Next 24 hours";
	public static final String TAB3 = "Next 10 days";
    public static final String LOOKUP_FAILED_STRING = "Location not found, try again";
    public static final String LOCATION_SEARCH_STRING = "Search for the weather at another city or airport";
    public static final String PREVIOUS_SEARCH_STRING = "Previous searches";
    // public static final String TOS_HEADING = "Terms of Service";
    // public static final String TOS_BODY = "Thanks for using Accessible Weather.\nDesigned and developed by the" +
    //         " Sonification Lab at The Georgia Institute of Technology.\nWeather data provided by Weather Underground.\n" +
    //         "Use of this application means you agree to be bound by the Terms of Service.";

    public static final String MENU_OPENED = "Menu";

    // Symbols and methods for the various units (metric and CRAZY). Just look at a world map of the countries that do not use metric system.
    public static final String UNICODE_PERCENT = "\u0025";
    public static final String UNICODE_DEGREE = "\u00B0";
    public static final int UNIT_TYPE_TEMP = 1;
    public static final int UNIT_TYPE_DISTANCE = 2;
    public static final int UNIT_TYPE_SPEED = 3;
    public static final int UNIT_TYPE_HEIGHT = 4;
    public static final int UNIT_TYPE_SNOW_HEIGHT = 5;

    public static final String METRIC_TEMP_SYMBOL = "C";
    public static final String CRAZY_TEMP_SYMBOL = "F";
    public static final String METRIC_TEMP_UNIT = "degree";
    public static final String CRAZY_TEMP_UNIT = "degree";
    public static final String METRIC_TEMP_UNITS = "degrees";
    public static final String CRAZY_TEMP_UNITS = "degrees";

    public static final String METRIC_DISTANCE_SYMBOL = "KM";
    public static final String CRAZY_DISTANCE_SYMBOL = "mi";
    public static final String METRIC_DISTANCE_UNIT = "Kilometer";
    public static final String CRAZY_DISTANCE_UNIT = "Mile";
    public static final String METRIC_DISTANCE_UNITS = "Kilometers";
    public static final String CRAZY_DISTANCE_UNITS = "Miles";

    public static final String METRIC_SPEED_SYMBOL = "KPH";
    public static final String CRAZY_SPEED_SYMBOL = "MPH";
    public static final String METRIC_SPEED_UNIT = "Kilometer per hour";
    public static final String CRAZY_SPEED_UNIT = "Mile per hour";
    public static final String METRIC_SPEED_UNITS = "Kilometers per hour";
    public static final String CRAZY_SPEED_UNITS = "Miles per hour";

    public static final String METRIC_HEIGHT_SYMBOL = "mm";
    public static final String METRIC_HEIGHT_SNOW_SYMBOL = "cm";
    public static final String CRAZY_HEIGHT_SYMBOL = "in";
    public static final String METRIC_HEIGHT_UNIT = "Milli meter";
    public static final String CRAZY_HEIGHT_UNIT = "Inch";
    public static final String METRIC_HEIGHT_UNITS = "Milli meters";
    public static final String CRAZY_HEIGHT_UNITS = "Inches";
    public static final String METRIC_HEIGHT_SNOW_UNIT = "Centimeter";
    public static final String METRIC_HEIGHT_SNOW_UNITS = "Centimeters";

    public static String GET_SYMBOL(int unitType, boolean isMetric)
    {
        switch(unitType)
        {
            case UNIT_TYPE_TEMP:
                if(isMetric)
                {
                    return UNICODE_DEGREE + METRIC_TEMP_SYMBOL;
                }
                else
                {
                    return UNICODE_DEGREE + CRAZY_TEMP_SYMBOL;
                }
            case UNIT_TYPE_DISTANCE:
                if(isMetric)
                {
                    return METRIC_DISTANCE_SYMBOL;
                }
                else
                {
                    return CRAZY_DISTANCE_SYMBOL;
                }
            case UNIT_TYPE_HEIGHT:
                if(isMetric)
                {
                    return METRIC_HEIGHT_SYMBOL;
                }
                else
                {
                    return CRAZY_HEIGHT_SYMBOL;
                }
            case UNIT_TYPE_SNOW_HEIGHT:
                if(isMetric)
                {
                    return METRIC_HEIGHT_SNOW_SYMBOL;
                }
                else
                {
                    return CRAZY_HEIGHT_SYMBOL;
                }
            case UNIT_TYPE_SPEED:
                if(isMetric)
                {
                    return METRIC_SPEED_SYMBOL;
                }
                else
                {
                    return CRAZY_SPEED_SYMBOL;
                }
            default:
                return "";
        }
    }

    public static String GET_READOUT(int unitValue, int unitType, boolean isMetric)
    {
        if(unitValue == 1)
        {
            switch(unitType)
            {
                case UNIT_TYPE_TEMP:
                    if(isMetric)
                    {
                        return METRIC_TEMP_UNIT;
                    }
                    else
                    {
                        return CRAZY_TEMP_UNIT;
                    }
                case UNIT_TYPE_DISTANCE:
                    if(isMetric)
                    {
                        return METRIC_DISTANCE_UNIT;
                    }
                    else
                    {
                        return CRAZY_DISTANCE_UNIT;
                    }
                case UNIT_TYPE_HEIGHT:
                    if(isMetric)
                    {
                        return METRIC_HEIGHT_UNIT;
                    }
                    else
                    {
                        return CRAZY_HEIGHT_UNIT;
                    }
                case UNIT_TYPE_SNOW_HEIGHT:
                    if(isMetric)
                    {
                        return METRIC_HEIGHT_SNOW_UNIT;
                    }
                    else
                    {
                        return CRAZY_HEIGHT_UNIT;
                    }
                case UNIT_TYPE_SPEED:
                    if(isMetric)
                    {
                        return METRIC_SPEED_UNIT;
                    }
                    else
                    {
                        return CRAZY_SPEED_UNIT;
                    }
                default:
                    return "";
            }
        }
        else
        {
            switch(unitType)
            {
                case UNIT_TYPE_TEMP:
                    if(isMetric)
                    {
                        return METRIC_TEMP_UNITS;
                    }
                    else
                    {
                        return CRAZY_TEMP_UNITS;
                    }
                case UNIT_TYPE_DISTANCE:
                    if(isMetric)
                    {
                        return METRIC_DISTANCE_UNITS;
                    }
                    else
                    {
                        return CRAZY_DISTANCE_UNITS;
                    }
                case UNIT_TYPE_HEIGHT:
                    if(isMetric)
                    {
                        return METRIC_HEIGHT_UNITS;
                    }
                    else
                    {
                        return CRAZY_HEIGHT_UNITS;
                    }
                case UNIT_TYPE_SNOW_HEIGHT:
                    if(isMetric)
                    {
                        return METRIC_HEIGHT_SNOW_UNITS;
                    }
                    else
                    {
                        return CRAZY_HEIGHT_UNITS;
                    }
                case UNIT_TYPE_SPEED:
                    if(isMetric)
                    {
                        return METRIC_SPEED_UNITS;
                    }
                    else
                    {
                        return CRAZY_SPEED_UNITS;
                    }
                default:
                    return "";
            }
        }
    }

    // Returns correct readout of wind directions
    public static String GET_WIND_DESC(String abbr)
    {
        if(abbr.equalsIgnoreCase("East"))
        {
            return "East";
        }
        else if(abbr.equalsIgnoreCase("ENE"))
        {
            return "East North East";
        }
        else if(abbr.equalsIgnoreCase("ESE"))
        {
            return "East South East";
        }
        else if(abbr.equalsIgnoreCase("NE"))
        {
            return "North East";
        }
        else if(abbr.equalsIgnoreCase("NNE"))
        {
            return "North North East";
        }
        else if(abbr.equalsIgnoreCase("NNW"))
        {
            return "North North West";
        }
        else if(abbr.equalsIgnoreCase("North"))
        {
            return "North";
        }
        else if(abbr.equalsIgnoreCase("NW"))
        {
            return "North West";
        }
        else if(abbr.equalsIgnoreCase("SE"))
        {
            return "South East";
        }
        else if(abbr.equalsIgnoreCase("South"))
        {
            return "South";
        }
        else if(abbr.equalsIgnoreCase("SSE"))
        {
            return "South South East";
        }
        else if(abbr.equalsIgnoreCase("SSW"))
        {
            return "South South West";
        }
        else if(abbr.equalsIgnoreCase("SW"))
        {
            return "South West";
        }
        else if(abbr.equalsIgnoreCase("Variable"))
        {
            return "Variable direction";
        }
        else if(abbr.equalsIgnoreCase("West"))
        {
            return "West";
        }
        else if(abbr.equalsIgnoreCase("WNW"))
        {
            return "West North West";
        }
        else if(abbr.equalsIgnoreCase("WSW"))
        {
            return "West South West";
        }
        else
        {
            return "Variable direction";
        }
    }

    public static int GET_CONDITION(String conditions)
    {
        if(conditions.equalsIgnoreCase("Clear") || conditions.equalsIgnoreCase("Sunny"))
        {
            return CONDITION_CLEAR_KEY;
        }
        else if(conditions.equalsIgnoreCase("Partly Cloudy") || conditions.equalsIgnoreCase("Mostly Sunny") || conditions.equalsIgnoreCase("Scattered Clouds"))
        {
            return CONDITION_PARTLY_CLOUDY_KEY;
        }
        else if(conditions.equalsIgnoreCase("Mostly Cloudy") || conditions.equalsIgnoreCase("Partly Sunny") || conditions.equalsIgnoreCase("Unknown"))
        {
            return CONDITION_MOSTLY_CLOUDY_KEY;
        }
        else if(conditions.equalsIgnoreCase("Overcast") || conditions.equalsIgnoreCase("Cloudy"))
        {
            return CONDITION_OVERCAST_KEY;
        }
        else if(conditions.equalsIgnoreCase("Chance of rain") || conditions.equalsIgnoreCase("Chance rain") || conditions.equalsIgnoreCase("Rain"))
        {
            return CONDITION_RAIN_KEY;
        }
        else if(conditions.equalsIgnoreCase("Chance of Freezing Rain") || conditions.equalsIgnoreCase("Chance of Sleet") || conditions.equalsIgnoreCase("Freezing Rain") || conditions.equalsIgnoreCase("Sleet"))
        {
            return CONDITION_SLEET_KEY;
        }
        else if(conditions.equalsIgnoreCase("Chance of Flurries") || conditions.equalsIgnoreCase("Flurries"))
        {
            return CONDITION_FLURRIES_KEY;
        }
        else if(conditions.equalsIgnoreCase("Chance of Snow") || conditions.equalsIgnoreCase("Snow"))
        {
            return CONDITION_SNOW_KEY;
        }
        else if(conditions.equalsIgnoreCase("Chance of Thunderstorms") || conditions.equalsIgnoreCase("Chance of a thunderstorm") || conditions.equalsIgnoreCase("Thunderstorms") || conditions.equalsIgnoreCase("Thunderstorm"))
        {
            return CONDITION_STORM_KEY;
        }
        else if(conditions.equalsIgnoreCase("Fog"))
        {
            return CONDITION_FOG_KEY;
        }
        else if(conditions.equalsIgnoreCase("Haze"))
        {
            return CONDITION_HAZE_KEY;
        }
        return CONDITION_DEFAULT_KEY;
    }
}
