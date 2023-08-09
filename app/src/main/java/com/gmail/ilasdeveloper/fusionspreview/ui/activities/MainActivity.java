package com.gmail.ilasdeveloper.fusionspreview.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.csv.CsvIndexer;
import com.gmail.ilasdeveloper.fusionspreview.data.models.BaseFragment;
import com.gmail.ilasdeveloper.fusionspreview.databinding.ActivityMainBinding;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.CombineFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.GuesserFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.RandomFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.SettingsFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.ShinyFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.Theming;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums.AppTheme;
import com.google.android.material.elevation.SurfaceColors;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private CombineFragment combineFragment;
    private GuesserFragment guesserFragment;
    private RandomFragment randomFragment;
    private ShinyFragment shinyFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;
    private FragmentManager fragmentManager;
    private ArrayList<String> monsList;
    private int lastId = -1;
    private CsvIndexer csvIndexer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null)
            return;

        updateTheme();

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        fragmentManager = getSupportFragmentManager();
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        combineFragment = new CombineFragment();
        guesserFragment = new GuesserFragment();
        randomFragment = new RandomFragment();
        shinyFragment = new ShinyFragment();
        settingsFragment = new SettingsFragment();

        init();
    }

    private void init() {
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                } else if (item.getItemId() == R.id.settings) {
                    updateFragment(settingsFragment);
                }
                lastId = item.getItemId();
                return true;
            }
            return false;
        });

        new Thread(() -> csvIndexer = CsvIndexer.createInstance(binding.getRoot().getContext(), "https://raw.githubusercontent.com/infinitefusion/sprites/main/Sprite%20Credits.csv")).start();

        getMons().thenAccept(strings -> {
            if (strings != null) {
                monsList = strings;
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("monsList", monsList);

                fragmentManager.beginTransaction()
                        .add(R.id.frame_layout, combineFragment, combineFragment.getClass().getName()).hide(combineFragment)
                        .add(R.id.frame_layout, guesserFragment, guesserFragment.getClass().getName()).hide(guesserFragment)
                        .add(R.id.frame_layout, randomFragment, randomFragment.getClass().getName()).hide(randomFragment)
                        .add(R.id.frame_layout, shinyFragment, shinyFragment.getClass().getName()).hide(shinyFragment)
                        .add(R.id.frame_layout, settingsFragment, settingsFragment.getClass().getName()).hide(settingsFragment)
                        .commit();

                combineFragment.setArguments(bundle);
                guesserFragment.setArguments(bundle);
                randomFragment.setArguments(bundle);
                shinyFragment.setArguments(bundle);
                updateFragment(combineFragment);
                lastId = R.id.combine;
            } else {
                Context context = this;
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.activity_error);
                        Button button = findViewById(R.id.retryButton);
                        button.setOnClickListener(view -> {
                            init();
                        });
                    }
                });
            }
        });
    }

    private void updateFragment(Fragment fragment) {
        if (activeFragment == null)
            activeFragment = guesserFragment;
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        this.activeFragment = fragment;
    }

    private ArrayList<String> retrieveMons() throws IOException {
        ArrayList<String> output = new ArrayList<>();

        String url = "https://raw.githubusercontent.com/IlasDev/InfiniteFusionData/main/mons.csv";
        String lastName = "";

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

        return output;
    }

    public void updateTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedTheme = prefs.getString("theme_scheme", AppTheme.DEFAULT.name());
        setTheme(Theming.getAppThemeFromName(selectedTheme).getThemeResId());
        updateMode();
    }

    public void updateMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedNightMode = prefs.getString("theme_mode", "system");
        updateMode(selectedNightMode);
    }

    public void updateMode(String selectedNightMode) {
        switch (selectedNightMode) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        };
    }

    @Override
    public void recreate() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (activeFragment instanceof BaseFragment) {
            ((BaseFragment) activeFragment).hideKeyboard(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public CsvIndexer getCsvIndexer() {
        return csvIndexer;
    }

    public CompletableFuture<ArrayList<String>> getMons() {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<String> output;
            try {
                output = retrieveMons();
            } catch (IOException e) {
                output = null;
            }
            return output;
        });
    }
}