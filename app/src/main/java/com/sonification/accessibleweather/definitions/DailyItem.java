package com.sonification.accessibleweather.definitions;

/**
 * This class defines the items in the next 10 days list.
 * Written by Jehoshaph Akshay Chandran 15 Jan 2015
 */
public class DailyItem
{
    String dailyTitle;
    String dailyCondition;
    String dailyHighs;
    String dailyLows;
    String dailyPOP;
    String dailySnow;

    public DailyItem(String dailyTitle, String dailyCondition, String dailyHighs, String dailyLows, String dailyPOP, String dailySnow)
    {
        this.dailyTitle = dailyTitle;
        this.dailyCondition = dailyCondition;
        this.dailyHighs = dailyHighs;
        this.dailyLows = dailyLows;
        this.dailyPOP = dailyPOP;
        this.dailySnow = dailySnow;
    }

    public String getDailyTitle()
    {
        return dailyTitle;
    }

    public String getDailyCondition()
    {
        return dailyCondition;
    }

    public String getDailyLows()
    {
        return dailyLows;
    }

    public String getDailyHighs()
    {
        return dailyHighs;
    }

    public String getDailyPOP()
    {
        return dailyPOP;
    }

    public String getDailySnow()
    {
        return dailySnow;
    }
}
