package eu.chylek.adam.fakewifi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import java.util.regex.Pattern;

/**
 * Created by chylek on 20.9.17.
 */

public class PrefsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static String PREF_MAC = "mac";
    public static String PREF_SSID = "ssid";
    public static String PREF_BSSID = "bssid";
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class MacCheck implements Preference.OnPreferenceChangeListener{
        final Pattern p = Pattern.compile("([0-9A-F]{2}:){5}[0-9A-F]{2}");
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String newString = ((String) newValue).trim().toUpperCase();
            boolean match = p.matcher(newString).matches() || newString.equals("");
            if (!match) {
                Toast.makeText(getActivity(), R.string.wrong_mac, Toast.LENGTH_LONG).show();
            }
            return match;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager pm  =getPreferenceManager();
        pm.setSharedPreferencesName(Utils.PREFERENCE_NAME); // use our preference file
        sp = pm.getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        setPreferencesFromResource(R.xml.prefs, rootKey); // use our subset of preferences
        updateSummary();

        findPreference(PREF_MAC).setOnPreferenceChangeListener(new MacCheck());
        findPreference(PREF_BSSID).setOnPreferenceChangeListener(new MacCheck());
    }

    private void updateSummary(){
        // check if MAC empty
        String mac = sp.getString(PREF_MAC,"");

        if (mac.equals("")) {
            findPreference(PREF_MAC).setSummary(R.string.summary_mac);
        }
        else {
            findPreference(PREF_MAC).setSummary(mac);
        }

        String bssid = sp.getString(PREF_BSSID,"");
        if (bssid.equals("")) {
            findPreference(PREF_BSSID).setSummary(R.string.summary_mac);
        }
        else {
            findPreference(PREF_BSSID).setSummary(bssid);
        }
        //check if SSID empty
        String ssid = sp.getString(PREF_SSID,"");

        if (ssid.equals("")) {
            findPreference(PREF_SSID).setSummary(R.string.summary_ssid);
        }
        else {
            findPreference(PREF_SSID).setSummary(ssid);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.fixPreferencePermission(getActivity());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateSummary();
    }
}
