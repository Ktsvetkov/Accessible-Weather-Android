package com.sonification.accessibleweather;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Displays the Help screen, includes option to contact dev.
 * Written by Jehoshaph Akshay Chandran on 29 March
 */
public class ActivityHelp extends Activity
{
    RelativeLayout helpSection01HeaderLayout;
    RelativeLayout helpSection02HeaderLayout;
    RelativeLayout helpSection03HeaderLayout;
    RelativeLayout helpSection04HeaderLayout;

    RelativeLayout aboutSection;
    RelativeLayout feedbackSection;

    TextView helpSection01BodyText;
    TextView helpSection02BodyText;
    TextView helpSection03BodyText;
    TextView helpSection04BodyText;

    private static boolean isOpen[] = new boolean[4];

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPage();
    }

    private void loadPage()
    {
        setContentView(R.layout.layout_help);

        helpSection01HeaderLayout = (RelativeLayout)findViewById(R.id.helpSection01HeaderLayout);
        helpSection02HeaderLayout = (RelativeLayout)findViewById(R.id.helpSection02HeaderLayout);
        helpSection03HeaderLayout = (RelativeLayout)findViewById(R.id.helpSection03HeaderLayout);
        helpSection04HeaderLayout = (RelativeLayout)findViewById(R.id.helpSection04HeaderLayout);
        aboutSection = (RelativeLayout)findViewById(R.id.aboutSection);
        feedbackSection = (RelativeLayout)findViewById(R.id.feedbackSection);

        helpSection01BodyText = (TextView)findViewById(R.id.helpSection01BodyText);
        helpSection02BodyText = (TextView)findViewById(R.id.helpSection02BodyText);
        helpSection03BodyText = (TextView)findViewById(R.id.helpSection03BodyText);
        helpSection04BodyText = (TextView)findViewById(R.id.helpSection04BodyText);

        helpSection01HeaderLayout.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Help section 01 clicked
                isOpen[0] = !isOpen[0];
                setVisibilities();
            }
        });
        helpSection02HeaderLayout.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Help section 02 clicked
                isOpen[1] = !isOpen[1];
                setVisibilities();
            }
        });
        helpSection03HeaderLayout.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Help section 03 clicked
                isOpen[2] = !isOpen[2];
                setVisibilities();
            }
        });
        helpSection04HeaderLayout.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Help section 04 clicked
                isOpen[3] = !isOpen[3];
                setVisibilities();
            }
        });
        aboutSection.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Starts the about activity
                Intent startAboutActivity = new Intent(ActivityHelp.this, ActivityAbout.class);
                startActivity(startAboutActivity);
            }
        });
        feedbackSection.setOnClickListener(new RelativeLayout.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Opens the play store link for feedback
                Uri uri = Uri.parse("market://details?id=" + ActivityHelp.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try
                {
                    startActivity(goToMarket);
                }
                catch(ActivityNotFoundException e)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + ActivityHelp.this.getPackageName())));
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        /*
        Initialize all help sections to closed
         */
        super.onResume();
        for(int i = 0; i < isOpen.length; i++)
        {
            isOpen[i] = false;
            setVisibilities();
        }
    }

    private void setVisibilities()
    {
        /*
        Sets the visibilities of all the text sections based on the openType values
         */
        if(isOpen[0])
        {
            helpSection01BodyText.setVisibility(View.VISIBLE);
            helpSection01BodyText.requestFocus();
        }
        else
        {
            helpSection01BodyText.setVisibility(View.GONE);
        }

        if(isOpen[1])
        {
            helpSection02BodyText.setVisibility(View.VISIBLE);
            helpSection02BodyText.requestFocus();
        }
        else
        {
            helpSection02BodyText.setVisibility(View.GONE);
        }

        if(isOpen[2])
        {
            helpSection03BodyText.setVisibility(View.VISIBLE);
            helpSection03BodyText.requestFocus();
        }
        else
        {
            helpSection03BodyText.setVisibility(View.GONE);
        }

        if(isOpen[3])
        {
            helpSection04BodyText.setVisibility(View.VISIBLE);
            helpSection04BodyText.requestFocus();
        }
        else
        {
            helpSection04BodyText.setVisibility(View.GONE);
        }
    }
}
