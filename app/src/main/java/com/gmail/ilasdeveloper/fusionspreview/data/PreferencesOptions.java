package com.gmail.ilasdeveloper.fusionspreview.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Locale;

public class PreferencesOptions {


    private static PreferencesOptions instance;
    private static Context context;

    private PreferencesOptions(Context context) {
        this.context = context;
    }

    public static PreferencesOptions getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesOptions(context);
        }
        return instance;
    }

    public static String getCDNUrl(int typeOfUrl, boolean isValidation) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String selected = isValidation ? prefs.getString("cdn", "jsdelivr") : prefs.getString("validation", "github");
        selected = selected.toUpperCase(Locale.ITALY);

        String url = "https://";
        String separator = "";
        if (selected.equals(ContentDeliveryNetwork.GITHUB.name())) {
            url += isValidation ? "github.com" : "raw.githubusercontent.com";
            separator = isValidation ? "/blob/" : "/";
        } else if (selected.equals(ContentDeliveryNetwork.JDELIVR.name())) {
            url += "cdn.jsdelivr.net/gh";
            separator = "@";
        } else {
            url += "cdn.statically.io/gh";
            separator = "/";
        }
        url += "/infinitefusion/";

        switch (typeOfUrl) {
            case 1:
                url += "sprites" + separator + "main/Other/Base%20Sprites/@param.png";
                url = "https://gitlab.com/infinitefusion/sprites/-/raw/master/Battlers/@param/@param.@param.png";
                break;
            case 2:
                url += "sprites" + separator + "main/CustomBattlers/@param1.@param2@param3.png";
                url = "https://gitlab.com/infinitefusion/sprites/-/raw/master/CustomBattlers/@param1/@param1.@param2@param3.png";
                break;
            default:
                url += "autogen-fusion-sprites" + separator + "master/Battlers/@param1/@param1.@param2.png";
                url = "https://gitlab.com/infinitefusion/sprites/-/raw/master/Battlers/@param1/@param1.@param2.png";
                break;
        }

        return url;
    }

    public enum ContentDeliveryNetwork {
        GITHUB,
        JDELIVR,
        STATICALLY;
    }
}
