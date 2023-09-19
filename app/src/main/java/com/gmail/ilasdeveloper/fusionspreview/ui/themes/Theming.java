package com.gmail.ilasdeveloper.fusionspreview.ui.themes;

import android.app.Activity;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums.AppTheme;

public class Theming {

    public static void applyTheme(Activity activity, AppTheme theme) {
        activity.setTheme(getThemeResId(theme));
    }

    public static int getThemeResId(AppTheme appTheme) {
        switch (appTheme) {
            // case MONET:
            //     return R.style.Theme_FusionsPreview_Monet;
            case OCEAN_BLUE:
                return R.style.Theme_FusionsPreview_Base;
            case GREEN_APPLE:
                return R.style.Theme_FusionsPreview_GreenApple;
            case LAVENDER:
                return R.style.Theme_FusionsPreview_Lavender;
            case MIDNIGHT_DUSK:
                return R.style.Theme_FusionsPreview_MidnightDusk;
            case STRAWBERRY_DAIQUIRI:
                return R.style.Theme_FusionsPreview_StrawberryDaiquiri;
            case TAKO:
                return R.style.Theme_FusionsPreview_Tako;
            case TEALTURQUOISE:
                return R.style.Theme_FusionsPreview_TealTurquoise;
            case YINYANG:
                return R.style.Theme_FusionsPreview_YinYang;
            case YOTSUBA:
                return R.style.Theme_FusionsPreview_Yotsuba;
            case TIDAL_WAVE:
                return R.style.Theme_FusionsPreview_TidalWave;
            default:
                return R.style.Theme_FusionsPreview;
        }
    }

    public static AppTheme getAppThemeFromName(String name) {
        for (AppTheme appTheme : AppTheme.values())
            if (appTheme.name().equals(name)) return appTheme;
        return null;
    }
}
