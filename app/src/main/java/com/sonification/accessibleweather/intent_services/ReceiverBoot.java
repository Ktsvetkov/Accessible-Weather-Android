package com.sonification.accessibleweather.intent_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This broadcast receiver is called when the phone boots up.
 * It starts the Notification Service in the background.
 * Written by Jehoshaph Akshay Chandran (29 Jan 2015)
 */
public class ReceiverBoot extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent startNotificationService = new Intent(context, IntentServiceNotifications.class);
        context.startService(startNotificationService);
    }
}
