package com.lukekorth.screennotifications;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lukekorth.mailable_log.MailableLog;
import com.lukekorth.screennotifications.helpers.AppHelper;

import java.util.Date;

public class ScreenNotificationsApplication extends Application {

    private static final String VERSION = "version";

    @Override
    public void onCreate() {
        super.onCreate();

        AppHelper.init(this);

        migrate();

        MailableLog.init(this, BuildConfig.DEBUG);
    }

    private void migrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int version = prefs.getInt(VERSION, 0);
        if (BuildConfig.VERSION_CODE > version) {
            String now = new Date().toString();

            prefs.edit()
                    .putString("upgrade_date", now)
                    .putInt(VERSION, BuildConfig.VERSION_CODE)
                    .apply();

            MailableLog.clearLog(this);
        }
    }
}
