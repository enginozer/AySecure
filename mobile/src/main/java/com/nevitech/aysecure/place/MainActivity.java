package com.nevitech.aysecure.place;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.google.android.gms.location.LocationRequest;
import com.nevitech.aysecure.R;



public class MainActivity extends AppCompatActivity {

    private static final double csLat                = 35.144569;
    private static final double csLon                = 33.411107;
    private static final float mInitialZoomLevel     = 19.0f;

    public static final String SHARED_PREFS_ANYPLACE = "Anyplace_Preferences";

    private final static int LOCATION_CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST               = 9001;
    private final static int SELECT_PLACE_ACTIVITY_RESULT                   = 1112;
    private final static int SEARCH_POI_ACTIVITY_RESULT                     = 1113;
    private final static int PREFERENCES_ACTIVITY_RESULT                    = 1114;

    // Location API
    private LocationClient mLocationClient;
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)

    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}
