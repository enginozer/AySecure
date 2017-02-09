package com.nevitech.aysecure.place;



import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;




import com.nevitech.aysecure.R;
import com.nevitech.aysecure.place.logger.NevitechLoggerActivity;


public class MainActivity extends AppCompatActivity

{
    Button b1;


    @Override
    public void onCreate(Bundle savedInstanceState)

    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=(Button)findViewById(R.id.btnGPSBul);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent i=new Intent(MainActivity.this, NevitechLoggerActivity.class);
                startActivity(i);
            }
        });

    }


}
