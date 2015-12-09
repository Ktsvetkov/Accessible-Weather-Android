package com.sonification.accessibleweather;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.sonification.accessibleweather.definitions.GlobalVariables;

/**
 * Activity that just displays the About page,
 * This activity takes care of handling link clicks
 * Written by Jehoshaph Akshay Chandran (29 Jan 2015)
 */
public class ActivityAbout extends Activity
{
    ImageView aboutWundergroundLogo;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPage();
    }

    private void loadPage()
    {
        setContentView(R.layout.layout_about);

        aboutWundergroundLogo = (ImageView)findViewById(R.id.aboutWundergroundLogo);

        aboutWundergroundLogo.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startWundergroundLink();
            }
        });
    }

    private void startWundergroundLink()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(GlobalVariables.WUNDERGROUND_REFERRAL_KEY));
        startActivity(intent);
    }
}
