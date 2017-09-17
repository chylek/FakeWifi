package eu.chylek.adam.fakewifi;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Fragment newFragment;
        if (isXposedInstallerAvailable(getApplicationContext())) {
            newFragment = new MainFragment();
        } else {
            newFragment = new XposedMissingFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, newFragment).commit();

    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /*
 * Check if the Xposed installer is installed and enabled on the device.
 *
 * @return {@code true} if the package "de.robv.android.xposed.installer" is installed and enabled.
 *
 *
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
    public static boolean isXposedInstallerAvailable(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo("de.robv.android.xposed.installer", 0);
            if (appInfo != null) {
                return appInfo.enabled;
            }
        } catch (PackageManager.NameNotFoundException  ignored) {
        }
        return false;
    }
}
