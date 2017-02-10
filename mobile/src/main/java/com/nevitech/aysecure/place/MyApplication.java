package com.nevitech.aysecure.place;

/**
 * Created by Emre on 23.1.2017.
 */
import android.app.Application;
import android.content.Context;

import com.nevitech.aysecure.place.utils.NevitechUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyApplication extends Application

{

    private static Context context;
    String msj="Mesaj"; //mac adresler ve diğer id ler dinamik olarak eklenecektir.

    public void onCreate()

    {

        super.onCreate();
        MyApplication.context = getApplicationContext();

    }

    public static Context getAppContext()

    {

        return MyApplication.context;

    }
    public void yazdır() throws IOException

    {

        File root = null;
        try

        {

            root = NevitechUtils.getRadioMapFoler(context,
                    "0",
                    "s");

        }catch (Exception e)

        {

            e.getMessage();

        }

        String filename_radiomap_download = NevitechUtils.getRadioMapFileName("0");
        String mean_fname                 = filename_radiomap_download;

        FileWriter out;

        out = new FileWriter(new File(root, mean_fname));
        out.write(msj);
        out.close();


    }



}
