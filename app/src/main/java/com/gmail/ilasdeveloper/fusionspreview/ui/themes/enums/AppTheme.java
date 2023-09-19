package com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums;

import com.gmail.ilasdeveloper.fusionspreview.R;

public enum AppTheme {

    DEFAULT(R.string.label_default, R.style.Theme_FusionsPreview),
    OCEAN_BLUE(R.string.theme_oceanblue, R.style.Theme_FusionsPreview_Base),
    // MONET(R.string.theme_monet, R.style.Theme_FusionsPreview_Monet),
    GREEN_APPLE(R.string.theme_greenapple, R.style.Theme_FusionsPreview_GreenApple),
    LAVENDER(R.string.theme_lavender, R.style.Theme_FusionsPreview_Lavender),
    MIDNIGHT_DUSK(R.string.theme_midnightdusk, R.style.Theme_FusionsPreview_MidnightDusk),
    STRAWBERRY_DAIQUIRI(R.string.theme_strawberrydaiquiri, R.style.Theme_FusionsPreview_StrawberryDaiquiri),
    TAKO(R.string.theme_tako, R.style.Theme_FusionsPreview_Tako),
    TEALTURQUOISE(R.string.theme_tealturquoise, R.style.Theme_FusionsPreview_TealTurquoise),
    TIDAL_WAVE(R.string.theme_tidalwave, R.style.Theme_FusionsPreview_TidalWave),
    YINYANG(R.string.theme_yinyang, R.style.Theme_FusionsPreview_YinYang),
    YOTSUBA(R.string.theme_yotsuba, R.style.Theme_FusionsPreview_Yotsuba);

    private Integer titleResId;
    private Integer themeResId;

    AppTheme(Integer titleResId, Integer themeResId) {
        this.titleResId = titleResId;
        this.themeResId = themeResId;
    }

    public Integer getTitleResId() {
        return titleResId;
    }

    public Integer getThemeResId() {
        return themeResId;
    }
}