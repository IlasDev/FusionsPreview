package com.gmail.ilasdeveloper.fusionspreview.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUrlValidator {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
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
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
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
