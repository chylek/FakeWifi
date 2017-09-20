package eu.chylek.adam.fakewifi;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by chylek on 20.9.17.
 */

public class PrefsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        getPreferenceManager().setSharedPreferencesName(Utils.PREFERENCE_NAME);
        setPreferencesFromResource(R.xml.prefs, rootKey);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.fixPreferencePermission(getActivity());
    }
}
