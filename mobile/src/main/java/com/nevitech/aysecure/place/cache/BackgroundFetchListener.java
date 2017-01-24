package com.nevitech.aysecure.place.cache;

/**
 * Created by Emre on 23.1.2017.
 */
import java.io.Serializable;

public class BackgroundFetchListener implements Serializable

{

    public enum ErrorType

    {

        EXCEPTION, CANCELLED, SINGLE_INSTANCE,

    }

    public enum Status

    {

        RUNNING, SUCCESS, STOPPED, // STOPPED {ERROR, CANCEL}

    }

    void onProgressUpdate(int progress_current,
                          int progress_total)

    {

    }

    void onErrorOrCancel(String result,
                         ErrorType error)

    {

    }

    void onSuccess(String result)

    {

    }

    void onPrepareLongExecute()

    {

    }

}
