package com.liteon.iview;

import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.StatusDialog;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private String mSystemMode;
	private StatusDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

            
            Intent intentActivity = new Intent();
            boolean isDVRReachable = intent.getBooleanExtra(Def.EXTRA_IS_DVR_REACHABLE, false);
            if (isDVRReachable) {
            	String mode = intent.getStringExtra(Def.EXTRA_GET_ALL_INFO);
                mSystemMode = mode;
	            if (mSystemMode.equals(Def.RECORDING_MODE)) {
	            	intentActivity.setClass(MainActivity.this, Preview.class);
	            } else if (mSystemMode.equals(Def.STORAGE_MODE)) {
	            	intentActivity.setClass(MainActivity.this, Records.class);
	            } 
            } else {
            	showVpnDialog();
            	return;
            }
            startActivity(intentActivity);
            finish();
        }
    };
    
    private void showVpnDialog() {
    	dialog = new StatusDialog(MainActivity.this, "iView is unable to establish a connection.", false, mOnClickListener);
    	dialog.setRetryMessage("Please Setup VPN again");
    	dialog.setOnDismissListener(mOnDismissListener);
    	dialog.show();
		
    }
    private OnDismissListener mOnDismissListener = new OnDismissListener() {
		
		@Override
		public void onDismiss(DialogInterface dialog) {
			finish();
		}
	};
	
	private android.view.View.OnClickListener mOnClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intentActivity = new Intent();
			intentActivity.setAction("android.net.vpn.SETTINGS");
        	intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intentActivity);
        	dialog.dismiss();
		}
	};
}