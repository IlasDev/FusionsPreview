package com.gmail.ilasdeveloper.fusionspreview.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.gmail.ilasdeveloper.fusionspreview.utils.updater.AppUpdater;
import com.gmail.ilasdeveloper.fusionspreview.utils.updater.UpdateListener;
import com.gmail.ilasdeveloper.fusionspreview.utils.updater.UpdateModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean analytics = prefs.getBoolean("crash", true);
        if (analytics) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
        }

        updateTheme();

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        SplashScreen.installSplashScreen(this);

        List<String> validInstallers = Arrays.asList("com.android.vending", "com.google.android.feedback");
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        boolean isFromPlayStore = installer != null && validInstallers.contains(installer);
        if (isFromPlayStore)
            showPlayStoreDialog();
        else
            checkUpdates(true);

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
            new Thread(CsvIndexer::getInstance).start();
        } else {
            new Thread(
                    () ->
                            csvIndexer =
                                    CsvIndexer.getInstance())
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
                                    lastId = R.id.combine;
                                    runOnUiThread(() -> {
                                        updateFragment(dexFragment);
                                        changeStatusBarColor(false);
                                    });
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

    private void showPlayStoreDialog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showDialog = preferences.getBoolean("showPlayStoreDialog", true);

        if (showDialog) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_moved, null);
            CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setView(dialogView);
            builder.setTitle(R.string.important_update);
            builder.setMessage(R.string.we_have_moved_our_app_to_github_releases_would_you_like_to_update_from_there_the_google_play_store_version_of_the_application_will_not_receive_updates);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IlasDev/FusionsPreview"));
                startActivity(browserIntent);
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                boolean doNotShowAgain = checkBox.isChecked();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("showPlayStoreDialog", !doNotShowAgain);
                editor.apply();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkUpdates(boolean usePrefs) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int showDialog = preferences.getInt("showUpdateDialog", -1);

        new AppUpdater(this, "https://cdn.jsdelivr.net/gh/IlasDev/InfiniteFusionData@main/updateInfo.json")
                .fetchUpdateModel()
                .thenAccept(updateModel -> {
                    if (usePrefs && showDialog == updateModel.getVersionCode())
                        return;
                    if (AppUpdater.getCurrentVersionCode(this) < updateModel.getVersionCode()) {
                        runOnUiThread(() -> {
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_moved, null);
                            CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
                            checkBox.setText(getString(R.string.skip_this_version));
                            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
                            if (usePrefs)
                                dialogBuilder.setView(dialogView);
                            dialogBuilder
                                    .setTitle(getString(R.string.update_available))
                                    .setMessage(getString(R.string.an_update_is_available_please_consider_updating_the_application_to_access_the_latest_features_and_bug_fixes))
                                    .setCancelable(updateModel.isCancellable())
                                    .setPositiveButton(getString(R.string.update), (dialog, which) -> {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateModel.getUrl()));
                                        startActivity(browserIntent);
                                    })
                                    .setNegativeButton(getString(R.string.close), (dialog, which) -> {
                                        if (usePrefs) {
                                            boolean doNotShowAgain = checkBox.isChecked();
                                            SharedPreferences.Editor editor = preferences.edit();
                                            if (doNotShowAgain)
                                                editor.putInt("showUpdateDialog", updateModel.getVersionCode());
                                            editor.apply();
                                        }
                                    }).show();
                        });
                    } else if (!usePrefs) {
                        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                                .setTitle(getString(R.string.no_update_available))
                                .setMessage(getString(R.string.great_news_you_re_using_the_latest_version))
                                .setNegativeButton(getString(R.string.close), (dialog, which) -> {})
                                .show();
                    }
                })
                .exceptionally(throwable -> {
                    // Handle exceptions here
                    return null;
                });
    }

}
