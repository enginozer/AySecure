package com.nevitech.aysecure.place;



import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



import com.nevitech.aysecure.R;
import com.nevitech.aysecure.place.gps.GPSTracker;


public class MainActivity extends AppCompatActivity

{

    Button         btnGps;
    TextView       latText;
    TextView       lonText;

    GPSTracker     gpsTracker;

    @Override
    public void onCreate(Bundle savedInstanceState)

    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latText = (TextView)findViewById(R.id.latText);
        lonText = (TextView)findViewById(R.id.lonText);
        btnGps  = (Button)findViewById(R.id.btnGPSBul);

        btnGps.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View v)

            {

                gpsTracker=new GPSTracker(MainActivity.this);

                if(gpsTracker.canGetLocation())

                {

                    double latitude  = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    String lat       = latitude+"";
                    String lon       = longitude+"";

                    latText.setText(lat);
                    lonText.setText(lon);
                    //Toast.makeText(getApplicationContext(),"enlem   :"+latitude+"boylam  :"+longitude,Toast.LENGTH_SHORT).show();

                }
                else

                {
                    gpsTracker.showSettingsAlert();

                }

            }

        });

    }


}
