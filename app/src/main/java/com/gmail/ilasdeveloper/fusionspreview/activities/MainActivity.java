package com.gmail.ilasdeveloper.fusionspreview.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.databinding.ActivityMainBinding;
import com.gmail.ilasdeveloper.fusionspreview.fragments.CombineFragment;
import com.gmail.ilasdeveloper.fusionspreview.fragments.GuesserFragment;
import com.gmail.ilasdeveloper.fusionspreview.fragments.RandomFragment;
import com.gmail.ilasdeveloper.fusionspreview.fragments.ShinyFragment;
import com.gmail.ilasdeveloper.fusionspreview.models.CustomFragment;
import com.google.android.material.elevation.SurfaceColors;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private CombineFragment combineFragment;
    private GuesserFragment guesserFragment;
    private RandomFragment randomFragment;
    private ShinyFragment shinyFragment;
    private CustomFragment activeFragment;
    private FragmentManager fragmentManager;
    private ArrayList<String> monsList;
    private int lastId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Picasso picasso = new Picasso.Builder(this).memoryCache(Cache.NONE).build();
        Picasso.setSingletonInstance(picasso);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        combineFragment = new CombineFragment();
        guesserFragment = new GuesserFragment();
        randomFragment = new RandomFragment();
        shinyFragment = new ShinyFragment();

        fragmentManager = getSupportFragmentManager();

        com.gmail.ilasdeveloper.fusionspreview.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView().getRootView(), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                if (imeVisible)
                    binding.bottomNavigation.setVisibility(View.GONE);
                else
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                return insets;
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            if (lastId != item.getItemId()) {
                if (item.getItemId() == R.id.combine) {
                    updateFragment(combineFragment);
                } else if (item.getItemId() == R.id.random) {
                    updateFragment(randomFragment);
                } else if (item.getItemId() == R.id.guess) {
                    updateFragment(guesserFragment);
                } else if (item.getItemId() == R.id.shiny) {
                    updateFragment(shinyFragment);
                }
                lastId = item.getItemId();
                return true;
            }
            return false;
        });

        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, combineFragment, combineFragment.getClass().getName()).hide(combineFragment)
                .add(R.id.frame_layout, guesserFragment, guesserFragment.getClass().getName()).hide(guesserFragment)
                .add(R.id.frame_layout, randomFragment, randomFragment.getClass().getName()).hide(randomFragment)
                .add(R.id.frame_layout, shinyFragment, shinyFragment.getClass().getName()).hide(shinyFragment)
                .commit();

        try {
            CompletableFuture.runAsync(() -> {
                monsList = retrieveMons();

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("monsList", monsList);
                combineFragment.setArguments(bundle);
                guesserFragment.setArguments(bundle);
                randomFragment.setArguments(bundle);
                shinyFragment.setArguments(bundle);
                updateFragment(combineFragment);
                lastId = R.id.combine;
            }).get();
        } catch (Exception ignored) {
        }
    }

    private void updateFragment(CustomFragment fragment) {
        if (activeFragment == null)
            activeFragment = guesserFragment;
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        this.activeFragment = fragment;
    }

    private ArrayList<String> retrieveMons() {
        ArrayList<String> output = new ArrayList<>();

        String url = "https://raw.githubusercontent.com/IlasDev/InfiniteFusionData/main/mons.csv";
        String lastName = "";

        try {
            URL rowdata = new URL(url);
            URLConnection data = rowdata.openConnection();
            Scanner input = new Scanner(data.getInputStream());

            while (input.hasNextLine() && output.size() < 649) {
                String line = input.nextLine();
                String name = line.split(",")[0];
                if (!lastName.equals(name)) {
                    output.add(name);
                    lastName = name;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    @SuppressLint("UnsafeIntentLaunch")
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        restartActivity();
    }
}