package com.liteon.iview;

import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private String mSystemMode;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = new Intent(MainActivity.this, Records.class);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_GET_ALL_INFO);
        registerReceiver(mBroadcastReceiver, intentFilter);
        //Get DVR info
		Intent intent = new Intent(getApplicationContext(), DvrInfoService.class);
		intent.setAction(Def.ACTION_GET_ALL_INFO);
		startService(intent);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }
	
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {

            String mode = intent.getStringExtra(Def.EXTRA_GET_ALL_INFO);
            mSystemMode = mode;
            Intent intentActivity = new Intent();
            if (mSystemMode.equals(Def.RECORDING_MODE)) {
            	intentActivity.setClass(MainActivity.this, Preview.class);
            } else if (mSystemMode.equals(Def.STORAGE_MODE)) {
            	intentActivity.setClass(MainActivity.this, Records.class);
            } else {
            	intentActivity.setAction("android.net.vpn.SETTINGS");
            	intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	Toast.makeText(context, "Please setup VPN to continue!!", Toast.LENGTH_LONG).show();
            }
            startActivity(intentActivity);
        }
    };
}