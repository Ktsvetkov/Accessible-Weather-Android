package com.sonification.accessibleweather.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sonification.accessibleweather.R;
import com.sonification.accessibleweather.definitions.HourlyItem;
import com.sonification.accessibleweather.definitions.ResourceHelper;

import java.util.List;

/**
 * This class creates an adapter that maps the values in HourlyItem to the hourly list.
 * Created by Jehoshaph Akshay Chandran (11 Jan 2015)
 */
public class CustomHourlyAdapter extends ArrayAdapter<HourlyItem>
{
    Activity context;
    List<HourlyItem> itemList;
    int layoutResID;

    public CustomHourlyAdapter(Activity context, int layoutResourceID, List<HourlyItem> listItems)
    {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.itemList = listItems;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ItemHolder itemHolder;
        View view = convertView;

        if(view == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            itemHolder = new ItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            itemHolder.rowTitleText = (TextView)view.findViewById(R.id.rowTitleText);
            itemHolder.rowConditionsText = (TextView)view.findViewById(R.id.rowConditionsText);
            itemHolder.rowTempText = (TextView)view.findViewById(R.id.rowTempText);
            itemHolder.rowPopText = (TextView)view.findViewById(R.id.rowPopText);
            itemHolder.hourlyRowLayout = (RelativeLayout)view.findViewById(R.id.hourlyRowLayout);
            itemHolder.rowIconImage = (ImageView)view.findViewById(R.id.rowIconImage);

            view.setTag(itemHolder);
        }
        else
        {
            itemHolder = (ItemHolder)view.getTag();
        }

        HourlyItem hourlyItem = this.itemList.get(position);

        itemHolder.rowTitleText.setText(hourlyItem.getHourlyTitle());
        itemHolder.rowConditionsText.setText(hourlyItem.getHourlyCondition());
        itemHolder.rowTempText.setText(hourlyItem.getHourlyTemp());
        itemHolder.rowPopText.setText(hourlyItem.getHourlyPOP());

        String contentDescription = hourlyItem.getHourlyTitle() + ", "
                                    + hourlyItem.getHourlyCondition() + ", "
                                    + hourlyItem.getHourlyTemp() + ", "
                                    + hourlyItem.getHourlyPOP();
        itemHolder.hourlyRowLayout.setContentDescription(contentDescription);
        itemHolder.rowIconImage.setImageResource(ResourceHelper.getHourlyIcon(hourlyItem.getHourlyCondition(), hourlyItem.getHourlyTime()));

        return view;
    }

    private static class ItemHolder
    {
        TextView rowTitleText;
        TextView rowConditionsText;
        TextView rowTempText;
        TextView rowPopText;
        RelativeLayout hourlyRowLayout;
        ImageView rowIconImage;
    }
}
