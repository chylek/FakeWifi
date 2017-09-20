package eu.chylek.adam.fakewifi;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;

/**
 * Created by chylek on 20.9.17.
 */

public class Utils {
    public static String PREFERENCE_NAME = "pref";

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
}
