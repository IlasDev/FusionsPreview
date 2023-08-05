package com.gmail.ilasdeveloper.fusionspreview;

import android.app.Application;
import android.util.Log;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

public class FusionsPreview extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso picasso = new Picasso.Builder(this).memoryCache(Cache.NONE).build();
        Picasso.setSingletonInstance(picasso);
    }
}
