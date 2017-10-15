package eu.chylek.adam.fakewifi;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
/*
    Copyright (C) https://github.com/AdBlocker-Reborn/AdBlocker_Reborn

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ContextUtils {

    public static Context getSystemContext() {
        return ActivityThread.currentActivityThread().getSystemContext();
    }

    public static Context getOwnContext() {
        try {
            return getSystemContext().createPackageContext("com.aviraxp.adblocker.continued", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}