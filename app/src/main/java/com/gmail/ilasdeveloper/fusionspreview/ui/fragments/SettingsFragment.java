package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.ui.activities.MainActivity;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.BuildConfig;

public class SettingsFragment extends PreferenceFragmentCompat   {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.appearance_preferences);

        Preference themeScheme = findPreference("theme_scheme");
        ListPreference themeMode = findPreference("theme_mode");
        Preference about = findPreference("about");
        Preference github = findPreference("github");
        Preference coffee = findPreference("coffee");
        Preference licenses = findPreference("licenses");

        themeMode.setOnPreferenceChangeListener((preference, newValue) -> {
            ((MainActivity) getContext()).updateMode((String) newValue);
            return true;
        });

        about.setOnPreferenceClickListener(preference -> {
            PackageInfo pInfo = null;
            try {
                pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) { }
            String version = pInfo.versionName;


            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext())
                    .setTitle("About")
                    .setMessage(
                            "FusionsPreview " + version  + "\n" +
                            "Build type: " + BuildConfig.BUILD_TYPE + "\n\n" +
                            "Source code: https://github.com/IlasDev/FusionsPreview" + "\n\n" +
                            "Developer's note: Please donate. I'm starving. Also this is a early" +
                                    " preview of the application. Much has to be improved, but" +
                                    " I'm lazy, so I'll take my time."
                    )
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
            dialogBuilder.show();
            return true;
        });

        github.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IlasDev/FusionsPreview"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        });

        coffee.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/ilasdev"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        });

        licenses.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
            return true;
        });

    }
}