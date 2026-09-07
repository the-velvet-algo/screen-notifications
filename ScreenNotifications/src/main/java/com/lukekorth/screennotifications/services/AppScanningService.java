package com.lukekorth.screennotifications.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;

import com.lukekorth.screennotifications.helpers.AppHelper;
import com.lukekorth.screennotifications.models.App;

import java.util.ArrayList;
import java.util.List;

public class AppScanningService extends IntentService {

    public static final String APPS_UPDATED_ACTION =
            "com.lukekorth.screennotifications.APPS_UPDATED";

    public AppScanningService() {
        super("AppScanningService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<App> updatedApps = new ArrayList<>(AppHelper.getNotifyingApps());
        ArrayList<String> previousAppPackages = new ArrayList<>();
        for (App app : updatedApps) {
            previousAppPackages.add(app.getPackageName());
        }

        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> applications = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applications) {
            if (!applicationInfo.enabled) {
                continue;
            }

            App app = null;
            for (App existing : updatedApps) {
                if (existing.getPackageName().equals(applicationInfo.packageName)) {
                    app = existing;
                    break;
                }
            }

            if (app == null) {
                app = new App();
                app.setPackageName(applicationInfo.packageName);
                updatedApps.add(app);
            }

            app.setName((String) applicationInfo.loadLabel(packageManager));

            previousAppPackages.remove(applicationInfo.packageName);
        }

        for (String uninstalledAppPackage : previousAppPackages) {
            for (int i = updatedApps.size() - 1; i >= 0; i--) {
                if (updatedApps.get(i).getPackageName().equals(uninstalledAppPackage)) {
                    updatedApps.remove(i);
                }
            }
        }

        AppHelper.saveApps(updatedApps);

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(APPS_UPDATED_ACTION));
    }
}
