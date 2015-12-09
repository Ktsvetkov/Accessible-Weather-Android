package com.sonification.accessibleweather.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sonification.accessibleweather.R;
import com.sonification.accessibleweather.definitions.DrawerItem;

import java.util.List;

/*
Custom Drawer Adapter that defines which elements to use to display the drawer items.
This is similar to a custom list adapter.
There is a small, not insignificant overhead to using this method.
 */
public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem>
{
    Activity context;
    List<DrawerItem> drawerItemList;
    int layoutResID;

    public CustomDrawerAdapter(Activity context, int layoutResourceID, List<DrawerItem> listItems)
    {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        DrawerItemHolder drawerHolder;
        View view = convertView;

        if(view == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.ItemName = (TextView)view.findViewById(R.id.itemText);
            drawerHolder.icon = (ImageView)view.findViewById(R.id.itemIcon);

            view.setTag(drawerHolder);
        }
        else
        {
            drawerHolder = (DrawerItemHolder)view.getTag();
        }

        DrawerItem dItem = this.drawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
        drawerHolder.ItemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerItemHolder
    {
        TextView ItemName;
        ImageView icon;
    }
}