package com.lukekorth.screennotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.lukekorth.screennotifications.adapters.AppAdapter;
import com.lukekorth.screennotifications.services.AppScanningService;

public class AppsActivity extends AppCompatActivity {

    private AppAdapter mAdapter;

    private final BroadcastReceiver mAppsUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.refresh();
        }
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.apps);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

        mAdapter = new AppAdapter(this);

		ListView listView = (ListView) findViewById(R.id.apps_list);
		listView.setAdapter(mAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(mAppsUpdatedReceiver,
                new IntentFilter(AppScanningService.APPS_UPDATED_ACTION));

        startService(new Intent(this, AppScanningService.class));
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.tearDown();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAppsUpdatedReceiver);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}

		return false;
	}
}
