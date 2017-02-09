package com.nevitech.aysecure.place.logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationRequest;
import com.nevitech.aysecure.R;
import com.nevitech.aysecure.place.nav.BuildingModel;
import com.nevitech.aysecure.place.sensors.MovementDetector;
import com.nevitech.aysecure.place.sensors.SensorsMain;
import com.nevitech.aysecure.place.wifi.SimpleWifiManager;
import com.nevitech.aysecure.place.wifi.WifiReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emre on 8.2.2017.
 */

public class NevitechLoggerActivity extends Activity

{
    public static final String     SHARED_PREFS_LOGGER  = "LoggerPreferences";
    private                        LoggerWiFi logger;
    private float                  raw_heading          = 0.0f;
    private boolean                walking              = false;
    private int                    mCurrentSamplesTaken = 0;
    private LocationRequest        mLocationRequest;

    private TextView               mTrackingInfoView    = null;
    private TextView               scanResults;
    private Button                 btnRecord;


    private MovementDetector  movementDetector;
    private SensorsMain       positioning;
    private ProgressDialog    mSamplingProgressDialog;
    private SimpleWifiManager wifi;
    private WifiReceiver      receiverWifi;
    private boolean           mIsSamplingActive = false;
    private SharedPreferences preferences;

    private BuildingModel     mCurrentBuilding  = null;


    @Override
    public void onCreate(Bundle bundle)

    {

        super.onCreate(bundle);
        setContentView(R.layout.log);

        btnRecord = (Button) findViewById(R.id.recordBtn);

        btnRecord.setOnClickListener(new View.OnClickListener()

        {

            public void onClick(View v)

            {
                //btnRecordingInfo();
            }

        });

        ImageButton btnFloorUp = (ImageButton) findViewById(R.id.btnFloorUp);

        btnFloorUp.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v)

            {


                if (mCurrentBuilding == null)

                {

                    Toast.makeText(getBaseContext(), "Load a map before tracking can be used!", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (mIsSamplingActive)

                {

                    Toast.makeText(getBaseContext(), "Invalid during logging.", Toast.LENGTH_LONG).show();
                    return;

                }

                // Move one floor up
                int index = mCurrentBuilding.getSelectedFloorIndex();

                if (mCurrentBuilding.checkIndex(index + 1))

                {

                   // bypassSelectBuildingActivity(mCurrentBuilding, mCurrentBuilding.getFloors().get(index + 1));
                }

            }

        });

        ImageButton btnFloorDown = (ImageButton) findViewById(R.id.btnFloorDown);

        btnFloorDown.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v)

            {

                if (mCurrentBuilding == null)

                {

                    Toast.makeText(getBaseContext(), "Load a map before tracking can be used!", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (mIsSamplingActive)

                {

                    Toast.makeText(getBaseContext(), "Invalid during logging.", Toast.LENGTH_LONG).show();
                    return;

                }

                // Move one floor down
                int index = mCurrentBuilding.getSelectedFloorIndex();

                if (mCurrentBuilding.checkIndex(index - 1))

                {
                   // bypassSelectBuildingActivity(mCurrentBuilding, mCurrentBuilding.getFloors().get(index - 1));
                }

            }

        });

        scanResults       = (TextView) findViewById(R.id.detectedAPs);
        mTrackingInfoView = (TextView) findViewById(R.id.trackingInfoData);

		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 2 seconds
        mLocationRequest.setInterval(2000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);

        PreferenceManager.setDefaultValues(this,
                                           SHARED_PREFS_LOGGER,
                                           MODE_PRIVATE,
                                           R.xml.preferences_logger,
                                           true);

        preferences = getSharedPreferences(SHARED_PREFS_LOGGER, MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
        onSharedPreferenceChanged(preferences, "walk_bar");

        String folder_browser = preferences.getString("folder_browser", null);

        if (folder_browser == null)

        {

            File f = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name));
            f.mkdirs();

            if (f.mkdirs() || f.isDirectory())

            {

                String                   path   = f.getAbsolutePath();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("folder_browser", path);
                editor.commit();

            }

        }
        else

        {

            File f = new File(folder_browser);
            f.mkdirs();

        }

        // WiFi manager to manage scans
        wifi = SimpleWifiManager.getInstance();
        // Create new receiver to get broadcasts
        receiverWifi = new SimpleWifiReceiver();
        wifi.registerScan(receiverWifi);
        wifi.startScan(preferences.getString("samples_interval", "1000"));

        positioning      = new SensorsMain(this);
        movementDetector = new MovementDetector();
        positioning.addListener(movementDetector);

        AnyPlaceLoggerReceiver mSamplingAnyplaceLoggerReceiver = new AnyPlaceLoggerReceiver();
        logger = new LoggerWiFi(mSamplingAnyplaceLoggerReceiver);


    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String            key)

    {
        // TODO Auto-generated method stub

        if (key.equals("walk_bar"))

        {

            int sensitivity = sharedPreferences.getInt("walk_bar", 26);
            int max         = Integer.parseInt(getResources().getString(R.string.walk_bar_max));
            MovementDetector.setSensitivity(max - sensitivity);

        }
        else

        if (key.equals("samples_interval"))

        {

            wifi.startScan(sharedPreferences.getString("samples_interval", "1000"));

        }

    }
    private void updateInfoView()

    {

        StringBuilder sb = new StringBuilder();
        sb.append("Lat[ ");
       // if (curLocation != null)
       //     sb.append(curLocation.latitude);
        sb.append(" ]");
        sb.append("\nLon[ ");
       // if (curLocation != null)
       //     sb.append(curLocation.longitude);
        sb.append(" ]");
        sb.append("\nHeading[ ");
        sb.append(String.format("%.2f", raw_heading));
        sb.append(" ]");
        sb.append("  Status[ ");
        sb.append(String.format("%8s", walking ? "Walking" : "Standing"));
        sb.append(" ]");
        sb.append("  Samples[ ");
        sb.append(mCurrentSamplesTaken);
        sb.append(" ]");
        mTrackingInfoView.setText(sb.toString());

    }



    public class AnyPlaceLoggerReceiver implements LoggerWiFi.Callback

    {

        @Override
        public void onFinish(LoggerWiFi          logger,
                             LoggerWiFi.Function function)

        {

            if (function == LoggerWiFi.Function.ADD)

            {

                runOnUiThread(new Runnable()

                {

                    public void run()

                    {

                        updateInfoView();

                    }

                });

            }
            else
            if (function == LoggerWiFi.Function.SAVE)

            {

                final boolean                            exceptionOccured = logger.exceptionOccured;
                final String                             msg              = logger.msg;
                final ArrayList<ArrayList<LogRecordMap>> mSamples         = logger.mSamples;

                runOnUiThread(new Runnable()

                {

                    @Override
                    public void run()

                    {

                        if (exceptionOccured)


                        {
                            Toast.makeText(NevitechLoggerActivity.this, msg, Toast.LENGTH_LONG).show();
                            return;

                        }
                        else

                        {

                            if (!(mSamples == null || mSamples.size() == 0))

                            {

                                ArrayList<LogRecordMap> prevSample = mSamples.get(0);
                                int sum = 0;

                                for (int i = 1; i < mSamples.size(); i++)

                                {

                                    ArrayList<LogRecordMap> records = mSamples.get(i);
                                    // double d = dist(prevSample.get(0).lat,
                                    // prevSample.get(0).lng,
                                    // records.get(0).lat, records.get(0).lng);

                                    if (records.get(0).walking)

                                    {
                                      //  LatLng latlng = new LatLng(prevSample.get(0).lat, prevSample.get(0).lng);
                                       // draw(latlng, sum);
                                        prevSample = records;

                                    }
                                    else

                                    {
                                        if (sum < 10)

                                        {

                                            sum += 1;

                                        }

                                    }

                                }

                              //  LatLng latlng = new LatLng(prevSample.get(0).lat, prevSample.get(0).lng);
                                //draw(latlng, sum);
                            }

                            Toast.makeText(NevitechLoggerActivity.this, mSamples.size() + " Samples Recorded Successfully!", Toast.LENGTH_LONG).show();

                        }

                        mCurrentSamplesTaken -= mSamples.size();

                        if (mSamplingProgressDialog != null)

                        {

                            mSamplingProgressDialog.dismiss();
                            mSamplingProgressDialog = null;
                          //  enableRecordButton();
                          //  showHelp("Help", "When you are done logging, click \"Menu\" -> \"Upload\"");

                        }

                    }

                });

            }

        }

    }

    private class SimpleWifiReceiver extends WifiReceiver

    {

        @Override
        public void onReceive(Context c, Intent intent)

        {

            // Log.d("SimpleWiFi Receiver", "wifi received");

            try

            {
                if (intent == null || c == null || intent.getAction() == null)

                {

                    return;

                }


                List<ScanResult> wifiList = wifi.getScanResults();
                scanResults.setText("AP : " + wifiList.size());

                // If we are not in an active sampling session we have to skip
                // this intent
                if (!mIsSamplingActive)
                    return;

                if (wifiList.size() > 0)

                {
                    mCurrentSamplesTaken++;

                    logger.add(wifiList, 0+" , "+0/*curLocation.latitude + "," + curLocation.longitude*/, raw_heading, walking);

                }
            }
            catch (RuntimeException e)

            {
                Toast.makeText(c, "RuntimeException [" + e.getMessage() + "]", Toast.LENGTH_SHORT).show();
                return;

            }

        }

    }

}
