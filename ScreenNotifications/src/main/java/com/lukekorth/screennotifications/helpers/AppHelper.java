package com.lukekorth.screennotifications.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.lukekorth.screennotifications.models.App;
import com.lukekorth.screennotifications.models.RecentApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppHelper {

    private static final String PREFS_NAME = "screen_notifications_data";
    private static final String APPS_KEY = "apps";
    private static final String RECENT_APPS_KEY = "recent_apps";
    private static final int MAX_RECENT_APPS = 200;

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    private static SharedPreferences prefs() {
        return sContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAppEnabled(String packageName) {
        for (App app : getNotifyingApps()) {
            if (app.getPackageName().equals(packageName)) {
                return app.getEnabled();
            }
        }
        return false;
    }

    public static synchronized void recordNotificationFromApp(String packageName) {
        List<App> apps = getNotifyingApps();
        for (App app : apps) {
            if (app.getPackageName().equals(packageName)) {
                return;
            }
        }

        App app = new App();
        app.setPackageName(packageName);
        app.setEnabled(true);
        apps.add(app);
        saveApps(apps);
    }

    public static synchronized void setAppEnabled(String packageName, boolean enabled) {
        List<App> apps = getNotifyingApps();
        for (App app : apps) {
            if (app.getPackageName().equals(packageName)) {
                app.setEnabled(enabled);
                break;
            }
        }
        saveApps(apps);
    }

    public static synchronized void recordScreenWakeFromApp(String packageName) {
        List<RecentApp> recentApps = getRecentNotifyingAppsUnsorted();

        RecentApp recentApp = new RecentApp();
        recentApp.setPackageName(packageName);
        recentApp.setTimestamp(System.currentTimeMillis());
        recentApps.add(recentApp);

        while (recentApps.size() > MAX_RECENT_APPS) {
            recentApps.remove(0);
        }

        saveRecentApps(recentApps);
    }

    public static synchronized List<RecentApp> getRecentNotifyingApps() {
        List<RecentApp> apps = getRecentNotifyingAppsUnsorted();
        Collections.sort(apps, new Comparator<RecentApp>() {
            @Override
            public int compare(RecentApp a, RecentApp b) {
                return Long.compare(b.getTimestamp(), a.getTimestamp());
            }
        });
        return apps;
    }

    public static synchronized List<App> getNotifyingApps() {
        List<App> apps = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs().getString(APPS_KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                App app = new App();
                app.setPackageName(obj.getString("packageName"));
                app.setName(obj.isNull("name") ? null : obj.optString("name", null));
                app.setEnabled(obj.optBoolean("enabled", false));
                apps.add(app);
            }
        } catch (JSONException e) {
            // corrupt/missing data, start fresh
        }

        Collections.sort(apps, new Comparator<App>() {
            @Override
            public int compare(App a, App b) {
                String nameA = a.getName() == null ? "" : a.getName();
                String nameB = b.getName() == null ? "" : b.getName();
                return nameA.compareToIgnoreCase(nameB);
            }
        });

        return apps;
    }

    public static synchronized void saveApps(List<App> apps) {
        JSONArray array = new JSONArray();
        try {
            for (App app : apps) {
                JSONObject obj = new JSONObject();
                obj.put("packageName", app.getPackageName());
                obj.put("name", app.getName());
                obj.put("enabled", app.getEnabled());
                array.put(obj);
            }
        } catch (JSONException e) {
            // ignore
        }

        prefs().edit().putString(APPS_KEY, array.toString()).apply();
    }

    private static List<RecentApp> getRecentNotifyingAppsUnsorted() {
        List<RecentApp> apps = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs().getString(RECENT_APPS_KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                RecentApp app = new RecentApp();
                app.setPackageName(obj.getString("packageName"));
                app.setTimestamp(obj.optLong("timestamp", 0));
                apps.add(app);
            }
        } catch (JSONException e) {
            // corrupt/missing data, start fresh
        }
        return apps;
    }

    private static void saveRecentApps(List<RecentApp> apps) {
        JSONArray array = new JSONArray();
        try {
            for (RecentApp app : apps) {
                JSONObject obj = new JSONObject();
                obj.put("packageName", app.getPackageName());
                obj.put("timestamp", app.getTimestamp());
                array.put(obj);
            }
        } catch (JSONException e) {
            // ignore
        }

        prefs().edit().putString(RECENT_APPS_KEY, array.toString()).apply();
    }
}
