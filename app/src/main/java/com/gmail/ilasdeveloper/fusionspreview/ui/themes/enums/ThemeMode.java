package com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums;

import androidx.appcompat.app.AppCompatDelegate;

public enum ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    public static void setAppCompatDelegateThemeMode(ThemeMode themeMode) {
        int appCompatMode;
        switch (themeMode) {
            case LIGHT:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case SYSTEM:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
            default:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_UNSPECIFIED;
        }
        AppCompatDelegate.setDefaultNightMode(appCompatMode);
    }
}