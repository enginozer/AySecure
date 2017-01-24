package com.nevitech.aysecure.place;

/**
 * Created by Emre on 23.1.2017.
 */
import android.app.Application;
import android.content.Context;

public class MyApplication extends Application

{

    private static Context context;

    public void onCreate()

    {

        super.onCreate();
        MyApplication.context = getApplicationContext();

    }

    public static Context getAppContext()

    {

        return MyApplication.context;

    }

}
