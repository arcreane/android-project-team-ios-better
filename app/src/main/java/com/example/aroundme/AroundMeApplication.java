package com.example.aroundme;

import android.app.Application;

import com.example.aroundme.utils.NotificationHelper;

public class AroundMeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createChannels(this);
    }
}