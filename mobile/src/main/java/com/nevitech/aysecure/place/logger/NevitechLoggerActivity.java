
package com.nevitech.aysecure.place.logger;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.nevitech.aysecure.R;
import com.nevitech.aysecure.place.NevitechAPI;
import com.nevitech.aysecure.place.nav.AnyUserData;
import com.nevitech.aysecure.place.nav.BuildingModel;
import com.nevitech.aysecure.place.sensors.MovementDetector;
import com.nevitech.aysecure.place.sensors.SensorsMain;
import com.nevitech.aysecure.place.utils.GeoPoint;
import com.nevitech.aysecure.place.wifi.SimpleWifiManager;
import com.nevitech.aysecure.place.wifi.WifiReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emre on 8.2.2017.
 */

public class NevitechLoggerActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener

{
    public static final String SHARED_PREFS_LOGGER = "LoggerPreferences";
    private LoggerWiFi logger;
    private float raw_heading = 0.0f;
    private boolean walking = false;
    private int mCurrentSamplesTaken = 0;
    private LocationRequest mLocationRequest;

    private TextView mTrackingInfoView = null;
    private LatLng curLocation = null;
    private Button btnRecord;
    private String folder_path_;
    private String filename_rss_;


    private ProgressDialog mSamplingProgressDialog;
    private SimpleWifiManager wifi;
    private WifiReceiver receiverWifi;
    private boolean mIsSamplingActive = false;
    private SharedPreferences preferences;

    private BuildingModel mCurrentBuilding = null;
    private LocationClient mLocationClient;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle bundle)

    {

        super.onCreate(bundle);
        setContentView(R.layout.log);
        checkPermission();
        verifyStoragePermissions(this);
        btnRecord = (Button) findViewById(R.id.recordBtn);

        btnRecord.setOnClickListener(new View.OnClickListener()

        {

            public void onClick(View v)

            {

                btnRecordingInfo();
            }

        });

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
        mLocationClient = new LocationClient(this, this, this);

        PreferenceManager.setDefaultValues(this,
                SHARED_PREFS_LOGGER,
                MODE_PRIVATE,
                R.xml.preferences_logger,
                true);

        preferences = getSharedPreferences(SHARED_PREFS_LOGGER, MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(preferences, "walk_bar");

        String folder_browser = preferences.getString("folder_browser", null);

        if (folder_browser == null)

        {

            File f = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name));
            f.mkdirs();

            if (f.mkdirs() || f.isDirectory())

            {

                String path = f.getAbsolutePath();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("folder_browser", path);
                editor.commit();

            }

        } else

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

        AnyPlaceLoggerReceiver mSamplingAnyplaceLoggerReceiver = new AnyPlaceLoggerReceiver();
        logger = new LoggerWiFi(mSamplingAnyplaceLoggerReceiver);


    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)

    {
        // TODO Auto-generated method stub

        if (key.equals("walk_bar"))

        {

            int sensitivity = sharedPreferences.getInt("walk_bar", 26);
            int max = Integer.parseInt(getResources().getString(R.string.walk_bar_max));
            MovementDetector.setSensitivity(max - sensitivity);

        } else if (key.equals("samples_interval"))

        {

            wifi.startScan(sharedPreferences.getString("samples_interval", "1000"));

        }

    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
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

    private void btnRecordingInfo()

    {

        if (mIsSamplingActive)

        {

            mIsSamplingActive = false;
            mSamplingProgressDialog = new ProgressDialog(NevitechLoggerActivity.this);
            mSamplingProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mSamplingProgressDialog.setMessage("Saving...");
            mSamplingProgressDialog.setCancelable(false);
            mSamplingProgressDialog.show();

            saveRecordingToLine(curLocation);

        } else

        {

            startRecordingInfo();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        wifi.unregisterScan(receiverWifi);
    }

    private void startRecordingInfo() {


        // avoid recording when no floor has been selected
        curLocation = new LatLng(40.917932, 29.3103284); // teknopark konumu el ile eklendi..

        if (curLocation == null) {
            Toast.makeText(getBaseContext(), "Click a position before recording...", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasGPS = getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        if (hasGPS) {
            if (true) {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (statusOfGPS == false) {
                    Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
                    return;
                }

                final GeoPoint gps;
                if (NevitechAPI.DEBUG_WIFI) {
                    gps = AnyUserData.fakeGPS();
                } else {
                    Location location = mLocationClient.getLastLocation();
                    if (location == null) {
                        Toast.makeText(this, "Waiting for a valid GPS signal", Toast.LENGTH_LONG).show();
                        return;
                    }
                    gps = new GeoPoint(location.getLatitude(), location.getLongitude());
                }

                //  mCurrentBuilding.longitude,mCurrentBuilding.latitude değerleri yerine manuel olarak değerler girildi..

                if (GeoPoint.getDistanceBetweenPoints(40.917932, 29.3103284, gps.dlon, gps.dlat, "") < 200) {
                    Toast.makeText(getBaseContext(), "You are only allowed to use the logger for a building you are currently at or physically nearby.", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }

        //Intent data = this.getIntent();
        //LoggerPrefs.Action result = (LoggerPrefs.Action) data.getSerializableExtra("action");
        File folder_path = new File(Environment.getExternalStorageDirectory() + File.separator + "Aysecure");
// have the object build the directory structure, if needed.
        folder_path_ = folder_path.toString();

        if (folder_path.equals("n/a") || folder_path.equals("")) {
            toastPrint("Folder path not specified\nGo to Menu::Preferences::Storing Settings::Folder", Toast.LENGTH_LONG);
            return;

        } else if ((!(new File(String.valueOf(folder_path)).canWrite()))) {
            toastPrint("Folder path is not writable\nGo to Menu::Preferences::Storing Settings::Folder", Toast.LENGTH_LONG);
            return;
        }

        File filename_rss = new File(folder_path + "deneme.txt"); //(String) preferences.getString("RSS_Log", "n/a");
        filename_rss_ = filename_rss.toString();
        if (filename_rss.equals("n/a") || filename_rss.equals("")) {
            toastPrint("Filename of RSS log not specified\nGo to Menu::Preferences::Storing Settings::Filename", Toast.LENGTH_LONG);
            return;
        }


        // start the TASK
        mIsSamplingActive = true;
    }

    private void saveRecordingToLine(LatLng latlng) {

        logger.save(latlng.latitude + "," + latlng.longitude, folder_path_, filename_rss_, "1", mCurrentBuilding.buid);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();

        // Flurry Analytics
        if (NevitechAPI.FLURRY_ENABLE) {
            FlurryAgent.onStartSession(this, NevitechAPI.FLURRY_APIKEY);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void toastPrint(String textMSG, int duration) {
        Toast.makeText(this, textMSG, duration).show();
    }

    public class AnyPlaceLoggerReceiver implements LoggerWiFi.Callback

    {

        @Override
        public void onFinish(LoggerWiFi logger,
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

            } else if (function == LoggerWiFi.Function.SAVE)

            {

                final boolean exceptionOccured = logger.exceptionOccured;
                final String msg = logger.msg;
                final ArrayList<ArrayList<LogRecordMap>> mSamples = logger.mSamples;

                runOnUiThread(new Runnable()

                {

                    @Override
                    public void run()

                    {

                        if (exceptionOccured)


                        {
                            Toast.makeText(NevitechLoggerActivity.this, msg, Toast.LENGTH_LONG).show();
                            return;

                        } else

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

                                    } else

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


                // If we are not in an active sampling session we have to skip
                // this intent
                if (!mIsSamplingActive)
                    return;

                if (wifiList.size() > 0)

                {
                    mCurrentSamplesTaken++;

                    logger.add(wifiList, 0 + " , " + 0/*curLocation.latitude + "," + curLocation.longitude*/, raw_heading, walking);

                }
            } catch (RuntimeException e)

            {
                Toast.makeText(c, "RuntimeException [" + e.getMessage() + "]", Toast.LENGTH_SHORT).show();
                return;

            }

        }

    }
}