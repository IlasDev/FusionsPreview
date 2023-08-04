package com.gmail.ilasdeveloper.fusionspreview.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.gmail.ilasdeveloper.fusionspreview.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        updateTheme();

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        combineFragment = new CombineFragment();
        guesserFragment = new GuesserFragment();
        randomFragment = new RandomFragment();
        shinyFragment = new ShinyFragment();
        settingsFragment = new SettingsFragment();

        fragmentManager = getSupportFragmentManager();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        // Breaks everything
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            View decorView = getWindow().getDecorView();
            WindowInsetsController windowInsetsController = decorView.getWindowInsetsController();
            windowInsetsController.addOnControllableInsetsChangedListener((windowInsetsController1, i) -> {
                if (i == 9)
                    binding.bottomNavigation.setVisibility(View.GONE);
                else
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
            });
        } */

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

        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, combineFragment, combineFragment.getClass().getName()).hide(combineFragment)
                .add(R.id.frame_layout, guesserFragment, guesserFragment.getClass().getName()).hide(guesserFragment)
                .add(R.id.frame_layout, randomFragment, randomFragment.getClass().getName()).hide(randomFragment)
                .add(R.id.frame_layout, shinyFragment, shinyFragment.getClass().getName()).hide(shinyFragment)
                .add(R.id.frame_layout, settingsFragment, settingsFragment.getClass().getName()).hide(settingsFragment)
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

    private void updateFragment(Fragment fragment) {
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
        startActivity(getIntent());
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (activeFragment instanceof BaseFragment) {
            ((BaseFragment) activeFragment).hideKeyboard(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}