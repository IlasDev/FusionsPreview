package com.gmail.ilasdeveloper.fusionspreview;

import android.app.Application;

import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.data.PreferencesOptions;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;

public class FusionsPreview extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso picasso = new Picasso.Builder(this).memoryCache(Cache.NONE).executor(Executors.newCachedThreadPool()).build();
        Picasso.setSingletonInstance(picasso);

        PreferencesOptions.getInstance(this);

        PokemonData.getInstance();
    }
}
