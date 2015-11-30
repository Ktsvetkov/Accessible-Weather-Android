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
import com.sonification.accessibleweather.definitions.DailyItem;
import com.sonification.accessibleweather.definitions.GlobalVariables;
import com.sonification.accessibleweather.definitions.ResourceHelper;

import java.util.List;

/**
 * This is the Custom adapter for the daily forecast list.
 * It acts as an interface between resources and each row of the list
 */
public class CustomDailyAdapter extends ArrayAdapter<DailyItem>
{
    Activity context;
    List<DailyItem> itemList;
    int layoutResID;

    public CustomDailyAdapter(Activity context, int layoutResourceID, List<DailyItem> listItems)
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
            itemHolder.dailyRowTitleText = (TextView)view.findViewById(R.id.dailyRowTitleText);
            itemHolder.dailyRowConditionsText = (TextView)view.findViewById(R.id.dailyRowConditionsText);
            itemHolder.dailyRowHighsText = (TextView)view.findViewById(R.id.dailyRowHighsText);
            itemHolder.dailyRowLowsText = (TextView)view.findViewById(R.id.dailyRowLowsText);
            itemHolder.dailyRowPopText = (TextView)view.findViewById(R.id.dailyRowPopText);
            itemHolder.dailyRowSnowText = (TextView)view.findViewById(R.id.dailyRowSnowText);
            itemHolder.dailyRowIconImage = (ImageView)view.findViewById(R.id.dailyRowIconImage);
            itemHolder.dailyRowLayout = (RelativeLayout)view.findViewById(R.id.dailyRowLayout);

            view.setTag(itemHolder);
        }
        else
        {
            itemHolder = (ItemHolder)view.getTag();
        }

        DailyItem dailyItem = this.itemList.get(position);

        itemHolder.dailyRowTitleText.setText(dailyItem.getDailyTitle());
        itemHolder.dailyRowConditionsText.setText(dailyItem.getDailyCondition());
        itemHolder.dailyRowHighsText.setText(dailyItem.getDailyHighs());
        itemHolder.dailyRowLowsText.setText(dailyItem.getDailyLows());
        itemHolder.dailyRowPopText.setText(dailyItem.getDailyPOP());
        itemHolder.dailyRowSnowText.setText(dailyItem.getDailySnow());

        String contentDescription = dailyItem.getDailyTitle() + ", "
                + dailyItem.getDailyCondition() + ", "
                + dailyItem.getDailyHighs() + ", "
                + dailyItem.getDailyLows() + ", "
                + dailyItem.getDailyPOP() + ", "
                + dailyItem.getDailySnow();
        itemHolder.dailyRowLayout.setContentDescription(contentDescription);
        if(dailyItem.getDailyTitle().toLowerCase().contains("night"))
        {
            itemHolder.dailyRowIconImage.setImageResource(ResourceHelper.getHourlyIcon(dailyItem.getDailyCondition(), GlobalVariables.NIGHT_START));
        }
        else
        {
            itemHolder.dailyRowIconImage.setImageResource(ResourceHelper.getHourlyIcon(dailyItem.getDailyCondition(), GlobalVariables.DAY_START));
        }

        return view;
    }

    private static class ItemHolder
    {
        TextView dailyRowTitleText;
        TextView dailyRowConditionsText;
        TextView dailyRowHighsText;
        TextView dailyRowLowsText;
        TextView dailyRowPopText;
        TextView dailyRowSnowText;
        ImageView dailyRowIconImage;
        RelativeLayout dailyRowLayout;
    }
}
