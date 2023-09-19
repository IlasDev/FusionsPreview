package com.gmail.ilasdeveloper.fusionspreview.utils.updater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AppUpdater {

    private static final String TAG = AppUpdater.class.getSimpleName();

    private final Context context;
    private final String jsonUrl;

    public AppUpdater(Context context, String jsonUrl) {
        this.context = context;
        this.jsonUrl = jsonUrl;
    }

    public CompletableFuture<UpdateModel> fetchUpdateModel() {
        return CompletableFuture.supplyAsync(() -> {
            if (context == null || jsonUrl == null) {
                Log.d(TAG, "Context or jsonUrl is null");
                throw new IllegalArgumentException("Context or jsonUrl is null");
            } else if (!isNetworkAvailable(context)) {
                throw new RuntimeException("Please check your network connection");
            } else if (jsonUrl.isEmpty()) {
                throw new RuntimeException("Please provide a valid JSON URL");
            }

            try {
                URL url = new URL(jsonUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }

                    Log.d(TAG, "JSON DATA: " + sb.toString());

                    JSONObject jsonObject = new JSONObject(sb.toString());

                    return new UpdateModel(
                            jsonObject.getInt("versionCode"),
                            jsonObject.getBoolean("cancellable"),
                            jsonObject.getString("url")
                    );
                } else {
                    throw new RuntimeException("Failed to fetch JSON data. Response code: " + responseCode);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int getCurrentVersionCode(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pInfo != null)
            return pInfo.versionCode;

        return 0;
    }
}
