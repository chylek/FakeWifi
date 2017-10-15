package eu.chylek.adam.fakewifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by chylek on 20.9.17.
 */

public class Utils {
    public static String PREFERENCE_NAME = "pref";
    public static String PREFERENCE_AUTHORITY = "eu.chylek.adam.fakewifi";
    public static String DEFAULT_SSID = "FakeWifi";
    public static String DEFAULT_BSSID = "11:22:33:44:55:66";
    public static String DEFAULT_MAC = "AA:BB:CC:DD:EE:FF";
    final static Pattern macMatcher = Pattern.compile("([0-9A-F]{2}:){5}[0-9A-F]{2}");

    /**
     * workaround for android N and later - preference files have to be in MODE_PRIVATE
     * so we need to override the permissions after each save so that Xposed part can read them.
     *
     * @param ctxt
     */
    @SuppressLint("SetWorldReadable")
    public static void fixPreferencePermission(Context ctxt){
        File prefsDir = new File(ctxt.getApplicationInfo().dataDir, "shared_prefs");
        File prefsFile = new File(prefsDir, PREFERENCE_NAME + ".xml");
        if (prefsFile.exists()) {
            prefsFile.setReadable(true, false);
        }

    }


    public static boolean checkMacFormat(String s){
        String newString = s.trim().toUpperCase();
        return macMatcher.matcher(newString).matches() || newString.equals("");
    }
}
