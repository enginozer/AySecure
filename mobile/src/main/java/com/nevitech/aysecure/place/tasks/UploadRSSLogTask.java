package com.nevitech.aysecure.place.tasks;

/**
 * Created by Emre on 7.2.2017.
 */

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import com.nevitech.aysecure.place.utils.ProgressHttpEntityWrapper;
import com.nevitech.aysecure.place.utils.ProgressHttpEntityWrapper.ProgressCallback;
import com.nevitech.aysecure.place.NevitechAPI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class UploadRSSLogTask extends AsyncTask<Void, Integer, String> {

    public interface UploadRSSLogTaskListener {
        void onErrorOrCancel(String result);

        void onSuccess(String result);
    }

    private UploadRSSLogTaskListener mListener;

    private String username, password;
    private String file;

    private Context context;
    private ProgressDialog dialog;
    private int currentProgress = 0;
    private HttpPost httppost;

    private boolean exceptionOccured = false;

    public UploadRSSLogTask(UploadRSSLogTaskListener l, Context ctx, String file, String username, String password){

        this.context = ctx;
        this.mListener = l;
        this.file = file;
        this.username = username;
        this.password = password;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMax(100);
        dialog.setMessage("Uploading file ...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                httppost.abort();
            }
        });
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            JSONObject j;
            j= new JSONObject();
            j.put("username", username);
            j.put("password", password);
            String json = j.toString();

            File rsslog = new File(this.file);
            if (rsslog.exists() == false) {
                exceptionOccured = true;
                return "File not found";
            }
            Log.d("radio upload", rsslog.toString());
            String response;

            HttpClient httpclient = new DefaultHttpClient();
            httppost = new HttpPost(NevitechAPI.getRadioUploadUrl());

            MultipartEntity entity = new MultipartEntity();
            entity.addPart("radiomap", new FileBody(rsslog));
            entity.addPart("json", new StringBody(json));

            ProgressCallback progressCallback = new ProgressCallback() {

                @Override
                public void progress(float progress) {
                    if (currentProgress != (int) (progress)) {
                        currentProgress = (int) progress;
                        publishProgress(currentProgress);
                    }
                }

            };

            httppost.setEntity(new ProgressHttpEntityWrapper(entity, progressCallback));
            HttpResponse httpresponse = httpclient.execute(httppost);
            HttpEntity resEntity = httpresponse.getEntity();

            response = EntityUtils.toString(resEntity);

            Log.d("radio upload", "response: " + response);

            j = new JSONObject(response);
            if (j.getString("status").equalsIgnoreCase("error")) {
                exceptionOccured = true;
                return "Error: " + j.getString("message");
            }

        } catch (JSONException e) {
            exceptionOccured = true;
            Log.d("upload rss log", e.getMessage());
            return "Cannot upload RSS log. JSONException occurred[ " + e.getMessage() + " ]";
        } catch (ParseException e) {
            exceptionOccured = true;
            Log.d("upload rss log", e.getMessage());
            return "Cannot upload RSS log. ParseException occurred[ " + e.getMessage() + " ]";
        } catch (IOException e) {
            exceptionOccured = true;
            Log.d("upload rss log", e.getMessage());

            if (httppost != null && httppost.isAborted()) {
                return "Uploading cancelled!";
            } else {
                return "Cannot upload RSS log. IOException occurred[ " + e.getMessage() + " ]";
            }

        }
        return "Successfully uploaded RSS log!";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        dialog.dismiss();

        if (exceptionOccured) {
            // call the error listener
            mListener.onErrorOrCancel(result);
        } else {
            // call the success listener
            mListener.onSuccess(result);
        }
    }

    @Override
    protected void onCancelled(String result) {
        mListener.onErrorOrCancel(result);
    }

    @Override
    protected void onCancelled() { // just for < API 11
        onCancelled("Uploading cancelled!");
    }

}
