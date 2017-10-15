package eu.chylek.adam.fakewifi;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import static eu.chylek.adam.fakewifi.Utils.PREFERENCE_AUTHORITY;
import static eu.chylek.adam.fakewifi.Utils.PREFERENCE_NAME;

/**
 * Created by Adam Chýlek on 11.10.2017.
 */

public class XposedPreferenceProvider extends RemotePreferenceProvider {
    public XposedPreferenceProvider() {
        super(PREFERENCE_AUTHORITY, new String[] {PREFERENCE_NAME});
    }
}