package eu.chylek.adam.fakewifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by Adam Chýlek on 09.09.2017.
 */

public class TaskerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.twofortyfouram.locale.intent.action.FIRE_SETTING".equals(intent.getAction())){
            Bundle bundle = intent.getBundleExtra(TaskerEditActivity.EXTRA_BUNDLE);
            boolean state = bundle.getBoolean(TaskerEditActivity.KEY_MASTER);
            SharedPreferences.Editor editor = context.getSharedPreferences(Utils.PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
            editor.putBoolean("master", state);
            putIfSet(TaskerEditActivity.KEY_BSSID, "bssid", bundle, editor);
            putIfSet(TaskerEditActivity.KEY_SSID, "ssid", bundle, editor);
            putIfSet(TaskerEditActivity.KEY_MAC, "mac", bundle, editor);
            editor.commit(); // do not use apply, otherwise the Xposed part of the module won't update its settings
            Utils.fixPreferencePermission(context);
        }
    }

    public void putIfSet(String key, String prefKey, Bundle bundle, SharedPreferences.Editor editor)
    {
        String s = bundle.getString(key,"");
        if (!"".equals(s)){
            editor.putString(prefKey,s);
        }
    }
}
