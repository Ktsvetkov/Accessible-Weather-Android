package com.sonification.accessibleweather.definitions;

/*
This class defines each drawer item's text and image view
 */
public class DrawerItem
{
    String ItemName;
    int imgResID;

    public DrawerItem(String itemName, int imgResID)
    {
        super();
        ItemName = itemName;
        this.imgResID = imgResID;
    }

    public String getItemName()
    {
        return ItemName;
    }

    public void setItemName(String itemName)
    {
        ItemName = itemName;
    }

    public int getImgResID()
    {
        return imgResID;
    }

    public void setImgResID(int imgResID)
    {
        this.imgResID = imgResID;
    }
}