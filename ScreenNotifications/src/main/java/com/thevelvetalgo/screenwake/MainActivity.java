package com.thevelvetalgo.screenwake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "screen_wake_prefs";
    public static final String ENABLED_KEY = "wake_enabled";

    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusText = findViewById(R.id.status_text);

        Button grantAccessButton = findViewById(R.id.grant_access_button);
        grantAccessButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        Switch enabledSwitch = findViewById(R.id.enabled_switch);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        enabledSwitch.setChecked(prefs.getBoolean(ENABLED_KEY, true));
        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(ENABLED_KEY, isChecked).apply());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        if (isNotificationAccessGranted()) {
            mStatusText.setText(R.string.access_granted);
        } else {
            mStatusText.setText(R.string.access_not_granted);
        }
    }

    private boolean isNotificationAccessGranted() {
        String enabledListeners = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(getPackageName());
    }
}
