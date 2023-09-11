package com.gmail.ilasdeveloper.fusionspreview.ui.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
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
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.DexFragment;
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
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private CombineFragment combineFragment;
    private GuesserFragment guesserFragment;
    private RandomFragment randomFragment;
    private ShinyFragment shinyFragment;
    private SettingsFragment settingsFragment;
    private DexFragment dexFragment;
    private Fragment activeFragment;
    private FragmentManager fragmentManager;
    private ArrayList<String> monsList;
    private int lastId = -1;
    private CsvIndexer csvIndexer;
    private boolean isLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        updateTheme();

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SplashScreen.installSplashScreen(this);

        fragmentManager = getSupportFragmentManager();
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        if (fragmentManager.getFragments().size() > 0) {
            combineFragment =
                    (CombineFragment)
                            fragmentManager.findFragmentByTag(CombineFragment.class.getName());
            guesserFragment =
                    (GuesserFragment)
                            fragmentManager.findFragmentByTag(GuesserFragment.class.getName());
            randomFragment =
                    (RandomFragment)
                            fragmentManager.findFragmentByTag(RandomFragment.class.getName());
            shinyFragment =
                    (ShinyFragment)
                            fragmentManager.findFragmentByTag(ShinyFragment.class.getName());
            settingsFragment =
                    (SettingsFragment)
                            fragmentManager.findFragmentByTag(SettingsFragment.class.getName());
            dexFragment =
                    (DexFragment)
                            fragmentManager.findFragmentByTag(DexFragment.class.getName());
        } else {
            combineFragment = new CombineFragment();
            guesserFragment = new GuesserFragment();
            randomFragment = new RandomFragment();
            shinyFragment = new ShinyFragment();
            settingsFragment = new SettingsFragment();
            dexFragment = new DexFragment();
        }

        if (savedInstanceState != null) {
            lastId = savedInstanceState.getInt("lastId");
        }

        init();
    }

    private void init() {
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isLoaded = false;

        binding.bottomNavigation.setOnItemSelectedListener(
                item -> {
                    if (!isLoaded) return false;
                    if (lastId != item.getItemId()) {
                        updateFragment(getFragmentById(item.getItemId()));
                        lastId = item.getItemId();
                        return true;
                    }
                    return false;
                });

        if (fragmentManager.getFragments().size() > 0) {
            isLoaded = true;
            updateFragment(getFragmentById(lastId));
        } else {
            new Thread(
                    () ->
                            csvIndexer =
                                    CsvIndexer.createInstance(
                                            "https://cdn.jsdelivr.net/gh/infinitefusion/sprites@main/Sprite%20Credits.csv"))
                    .start();

            getMons()
                    .thenAccept(
                            strings -> {
                                if (strings != null) {
                                    monsList = strings;
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArrayList("monsList", monsList);

                                    fragmentManager
                                            .beginTransaction()
                                            .add(
                                                    R.id.frame_layout,
                                                    combineFragment,
                                                    combineFragment.getClass().getName())
                                            .hide(combineFragment)
                                            .add(
                                                    R.id.frame_layout,
                                                    guesserFragment,
                                                    guesserFragment.getClass().getName())
                                            .hide(guesserFragment)
                                            .add(
                                                    R.id.frame_layout,
                                                    randomFragment,
                                                    randomFragment.getClass().getName())
                                            .hide(randomFragment)
                                            .add(
                                                    R.id.frame_layout,
                                                    shinyFragment,
                                                    shinyFragment.getClass().getName())
                                            .hide(shinyFragment)
                                            .add(
                                                    R.id.frame_layout,
                                                    settingsFragment,
                                                    settingsFragment.getClass().getName())
                                            .hide(settingsFragment)
                                            .add(
                                                    R.id.frame_layout,
                                                    dexFragment,
                                                    dexFragment.getClass().getName())
                                            .hide(dexFragment)
                                            .commit();

                                    combineFragment.setArguments(bundle);
                                    guesserFragment.setArguments(bundle);
                                    randomFragment.setArguments(bundle);
                                    shinyFragment.setArguments(bundle);
                                    dexFragment.setArguments(bundle);

                                    isLoaded = true;
                                    updateFragment(dexFragment);
                                    lastId = R.id.combine;
                                    changeStatusBarColor(false);
                                } else {
                                    this.runOnUiThread(
                                            () -> {
                                                setContentView(R.layout.activity_error);
                                                Button button = findViewById(R.id.retryButton);
                                                button.setOnClickListener(
                                                        view -> {
                                                            init();
                                                        });
                                            });
                                }
                            });
        }
    }

    private void updateFragment(Fragment fragment) {
        if (activeFragment == null) activeFragment = guesserFragment;
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        this.activeFragment = fragment;
        changeStatusBarColor(fragment != dexFragment);
    }

    private ArrayList<String> retrieveMons() throws IOException {
        ArrayList<String> output = new ArrayList<>();

        String url = "https://cdn.jsdelivr.net/gh/IlasDev/InfiniteFusionData@main/mons.csv";
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
        setTheme(
                Objects.requireNonNull(Theming.getAppThemeFromName(selectedTheme))
                        .getThemeResId());
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
        }
    }

    public void changeStatusBarColor(boolean reset) {
        Window window = getWindow();
        if (reset) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
            return;
        }
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        int color = a.data;
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (activeFragment instanceof BaseFragment) {
            ((BaseFragment) activeFragment).hideKeyboard(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public CompletableFuture<ArrayList<String>> getMons() {
        return CompletableFuture.supplyAsync(
                () -> {
                    ArrayList<String> output;
                    try {
                        output = retrieveMons();
                    } catch (IOException e) {
                        output = null;
                    }
                    return output;
                });
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastId", lastId);
    }

    private Fragment getFragmentById(int id) {
        if (id == R.id.combine) {
            return dexFragment;
        } else if (id == R.id.random) {
            return randomFragment;
        } else if (id == R.id.guess) {
            return guesserFragment;
        } else if (id == R.id.shiny) {
            return shinyFragment;
        } else if (id == R.id.settings) {
            return settingsFragment;
        }
        return null;
    }
}
