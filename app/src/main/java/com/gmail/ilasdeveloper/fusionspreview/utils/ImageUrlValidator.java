package com.gmail.ilasdeveloper.fusionspreview.utils;

import android.os.Handler;
import android.os.Looper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUrlValidator {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void validateImageUrl(String imageUrl, ImageUrlValidationListener listener) {
        executorService.execute(() -> {
            boolean isValid = checkUrlIsValid(imageUrl);
            runOnUIThread(() -> {
                if (listener != null) {
                    listener.onImageUrlValidationResult(isValid);
                }
            });
        });
    }

    private static boolean checkUrlIsValid(String imageUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(imageUrl)
                    .head()
                    .build();
            Response response = client.newCall(request).execute();
            int responseCode = response.code();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void runOnUIThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public interface ImageUrlValidationListener {
        void onImageUrlValidationResult(boolean isValid);
    }
}
