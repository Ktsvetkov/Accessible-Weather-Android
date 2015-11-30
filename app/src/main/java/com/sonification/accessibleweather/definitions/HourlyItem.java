package com.sonification.accessibleweather.definitions;

/*
This class defines each hour forecast, this is used to store a forecast
object which is used to populate the list on the next 24 hours page.
-Created by Jehoshaph Akshay Chandran (11 Jan 2015)
 */
public class HourlyItem
{
    String hourlyTitle;
    String hourlyCondition;
    String hourlyTemp;
    String hourlyPOP;
    int hourlyTime;

    public HourlyItem(String hourlyTitle, String hourlyCondition, String hourlyTemp, String hourlyPOP, int hourlyTime)
    {
        this.hourlyTitle = hourlyTitle;
        this.hourlyCondition = hourlyCondition;
        this.hourlyTemp = hourlyTemp;
        this.hourlyPOP = hourlyPOP;
        this.hourlyTime = hourlyTime;
    }

    public String getHourlyTitle()
    {
        return hourlyTitle;
    }

    public void setHourlyTitle(String hourlyTitle)
    {
        this.hourlyTitle = hourlyTitle;
    }

    public String getHourlyCondition()
    {
        return hourlyCondition;
    }

    public void setHourlyCondition(String hourlyCondition)
    {
        this.hourlyCondition = hourlyCondition;
    }

    public String getHourlyTemp()
    {
        return hourlyTemp;
    }

    public void setHourlyTemp(String hourlyTemp)
    {
        this.hourlyTemp = hourlyTemp;
    }

    public String getHourlyPOP()
    {
        return hourlyPOP;
    }

    public void setHourlyPOP(String hourlyPOP)
    {
        this.hourlyPOP = hourlyPOP;
    }

    public int getHourlyTime()
    {
        return hourlyTime;
    }

    public void setHourlyTime(int hourlyTime)
    {
        this.hourlyTime = hourlyTime;
    }
}