package eu.chylek.adam.fakewifi;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TaskerEditActivity extends AppCompatActivity {
    final static String KEY_MASTER = "eu.chylek.adam.fakewifi.KEY_MASTER";
    final static String KEY_MAC = "eu.chylek.adam.fakewifi.KEY_MAC";
    final static String KEY_SSID = "eu.chylek.adam.fakewifi.KEY_SSID";
    final static String KEY_BSSID= "eu.chylek.adam.fakewifi.KEY_BSSID";
    final static String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    final static String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";
    Bundle prefBundle = new Bundle();

    private class EditClickListener implements View.OnClickListener {
        private boolean checkMac = false;
        private final String title;
        private final String message;
        private String value;
        private String key;

        EditClickListener(boolean isMac, String key, String value, int title, int message) {
            checkMac=isMac;
            Resources r = getResources();
            this.title = r.getString(title);
            this.message = r.getString(message);
            this.value = value;
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void onClick(final View view) {
            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext(), R.style.Theme_AppCompat_Dialog_Alert);
            final EditText edittext = new EditText(view.getContext());
            edittext.setText(value);
            alert.setMessage(message);
            alert.setTitle(title);

            alert.setView(edittext);

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = edittext.getText().toString();
                    if (checkMac && !Utils.checkMacFormat(value)) {
                        Toast.makeText(view.getContext(), R.string.wrong_mac, Toast.LENGTH_LONG).show();
                    }
                    else {
                        setValue(value);
                        prefBundle.putString(key, value);
                        fillDescriptions();
                    }
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    edittext.setText(value);
                    dialog.dismiss();
                }
            });

            alert.show();
        }
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Bundle localeBundle = getIntent().getBundleExtra(EXTRA_BUNDLE);

        if (null == savedInstanceState && localeBundle != null) {
            final boolean state = localeBundle.getBoolean(KEY_MASTER, false);
            ((SwitchCompat) findViewById(R.id.masterswitch)).setChecked(state);
            checkKey(KEY_MAC, localeBundle);
            checkKey(KEY_SSID, localeBundle);
            checkKey(KEY_BSSID, localeBundle);
            fillDescriptions();
        }

        attachListeners();
    }

    private String checkKey(String key, Bundle bundle){
        String s = bundle.getString(key, "");
        prefBundle.putString(key, s);
        return s;
    }

    private void attachListeners(){
        findViewById(R.id.mac).setOnClickListener(new EditClickListener(true, KEY_MAC, prefBundle.getString(KEY_MAC), R.string.title_mac, R.string.dialog_mac));
        findViewById(R.id.ssid).setOnClickListener(new EditClickListener(false, KEY_SSID, prefBundle.getString(KEY_SSID), R.string.title_ssid, R.string.dialog_ssid));
        findViewById(R.id.bssid).setOnClickListener((new EditClickListener(true, KEY_BSSID, prefBundle.getString(KEY_BSSID), R.string.title_bssid, R.string.dialog_mac)));
    }

    private void fillDescription(View v, String key, int defaultText){
        String s = prefBundle.getString(key, "");
        if ("".equals(s)){
            ((TextView) v.findViewById(R.id.description)).setText(defaultText);
        }
        else {
            ((TextView) v.findViewById(R.id.description)).setText(s);
        }
    }
    private void fillDescriptions(){
        fillDescription(findViewById(R.id.mac),KEY_MAC, R.string.summary_mac);
        fillDescription(findViewById(R.id.ssid),KEY_SSID, R.string.summary_ssid);
        fillDescription(findViewById(R.id.bssid),KEY_BSSID, R.string.summary_bssid);
    }

    @Override
    public void finish() {
        boolean state = ((ToggleButton) findViewById(R.id.masterswitch)).isChecked();

        final Intent resultIntent = new Intent();

        /*
         * Bundle that Tasker / Locale will send
         */
        prefBundle.putBoolean(KEY_MASTER, state);
        resultIntent.putExtra(EXTRA_BUNDLE, prefBundle);

        /*
         * The blurb is concise status text to be displayed in the host's UI.
         */
        final String blurb = getResources().getString(state ? R.string.master_switch_on : R.string.master_switch_off);
        resultIntent.putExtra(EXTRA_STRING_BLURB, blurb);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }
}
