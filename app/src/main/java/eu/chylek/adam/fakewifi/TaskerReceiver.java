package eu.chylek.adam.fakewifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Adam Chýlek on 09.09.2017.
 */

public class TaskerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.twofortyfouram.locale.intent.action.FIRE_SETTING".equals(intent.getAction())){
            boolean state = intent.getBundleExtra(TaskerEditActivity.EXTRA_BUNDLE).getBoolean(TaskerEditActivity.EXTRA_MASTER);
            SharedPreferences.Editor editor = context.getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
            editor.putBoolean("master", state);
            editor.commit(); // do not use apply, otherwise the Xposed part of the module won't update its settings
            MainFragment.fixPreferencePermission(context);
        }
    }
}
