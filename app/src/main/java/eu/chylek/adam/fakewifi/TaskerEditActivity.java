package eu.chylek.adam.fakewifi;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;

public class TaskerEditActivity extends AppCompatActivity {
    final static String EXTRA_MASTER = "eu.chylek.adam.fakewifi.EXTRA_MASTER";
    final static String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    final static String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final Bundle localeBundle = getIntent().getBundleExtra(EXTRA_BUNDLE);

        if (null == savedInstanceState && localeBundle != null) {
            final boolean state = localeBundle.getBoolean(EXTRA_MASTER, false);
            ((ToggleButton) findViewById(R.id.masterswitch)).setChecked(state);
        }
    }

    @Override
    public void finish() {
        boolean state = ((ToggleButton) findViewById(R.id.masterswitch)).isChecked();

        final Intent resultIntent = new Intent();

        /*
         * Bundle that Tasker / Locale will send
         */
        final Bundle resultBundle =
                new Bundle();
        resultBundle.putBoolean(EXTRA_MASTER, state);
        resultIntent.putExtra(EXTRA_BUNDLE, resultBundle);

        /*
         * The blurb is concise status text to be displayed in the host's UI.
         */
        final String blurb = getResources().getString(state ? R.string.master_switch_on : R.string.master_switch_off);
        resultIntent.putExtra(EXTRA_STRING_BLURB, blurb);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }
}
