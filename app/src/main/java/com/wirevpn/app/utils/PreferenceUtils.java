package com.wirevpn.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper for reading/writing app preferences.
 */
public class PreferenceUtils {

    private static final String PREFS_NAME       = "wirevpn_prefs";
    private static final String KEY_WAS_CONNECTED = "was_connected";

    public static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setWasConnected(Context context, boolean value) {
        getPrefs(context).edit()
                .putBoolean(KEY_WAS_CONNECTED, value)
                .apply();
    }

    public static boolean wasConnected(Context context) {
        return getPrefs(context).getBoolean(KEY_WAS_CONNECTED, false);
    }
}
